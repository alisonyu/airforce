package com.alisonyu.airforce.microservice.meta;

import com.alisonyu.airforce.constant.CallMode;
import com.alisonyu.airforce.constant.ContentTypes;
import com.alisonyu.airforce.constant.ParamType;
import com.alisonyu.airforce.constant.Strings;
import com.alisonyu.airforce.microservice.anno.BodyParam;
import com.alisonyu.airforce.microservice.anno.SessionParam;
import com.alisonyu.airforce.microservice.anno.Sync;
import com.alisonyu.airforce.microservice.core.param.ParamMeta;
import com.alisonyu.airforce.tool.*;
import com.alisonyu.airforce.tool.instance.Anno;
import com.alisonyu.airforce.tool.instance.Reflect;
import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rest接口方法元信息
 * @author yuzhiyi
 * @date 2018/9/12 10:47
 */
public class RouteMeta {

	Logger logger = LoggerFactory.getLogger(RouteMeta.class);

	/**
	 * 挂载的路径
	 */
	private String path;
	/**
	 * 参数元数据
 	 */
	private ParamMeta[] paramMetas;
	/**
	* 返回值
	 */
	private Class<?> returnType;
	/**
	 * 返回值content-type
	 */
	private String produceType;
	/**
	 * Http方法,默认为GET
	 */
	private io.vertx.core.http.HttpMethod httpMethod = io.vertx.core.http.HttpMethod.GET;
	/**
	 * 调用模式
	 */
	private CallMode mode;
	/**
	 * 被调用的对象
	 */
	private Object proxy;
	/**
	 * 被调用的方法
	 */
	private Method proxyMethod;
	/**
	 * 方法的名称
	 */
	private String methodName;


	public RouteMeta(String rootPath,Method method){
		this.methodName = method.getName();
		this.returnType = method.getReturnType();
		this.proxyMethod = method;
		initProduceType(method);
		initHttpMethod(method);
		initMode(method);
		initParamMetas(method);
		initPath(rootPath, method);
	}

	private void initHttpMethod(Method method){
		Arrays.stream(method.getAnnotations())
				.filter(annotation -> annotation.annotationType().isAnnotationPresent(javax.ws.rs.HttpMethod.class))
				.findFirst()
				.map(Annotation::annotationType)
				.ifPresent(anno-> this.httpMethod = Functions.match(anno,
						Case.of(GET.class,()-> io.vertx.core.http.HttpMethod.GET ),
						Case.of(POST.class,()->io.vertx.core.http.HttpMethod.POST),
						Case.of(PUT.class,()-> io.vertx.core.http.HttpMethod.PUT),
						Case.of(DELETE.class,()->io.vertx.core.http.HttpMethod.DELETE)));
	}

	private void initMode(Method method){
		Annotation anno = method.getAnnotation(Sync.class);
		Class<?> returnType = method.getReturnType();
		if (Flowable.class.isAssignableFrom(returnType) || Future.class.isAssignableFrom(returnType)) {
			this.mode = anno == null ? CallMode.ASYNC : CallMode.SYNC;
		}
		//如果非 Flowable或者Future 返回类型的 并且没有@SYNC，那么报错
		else{
			if (anno == null){
				logger.error("{} should be run in sync,please use @Sync",method.toGenericString());
				throw new IllegalArgumentException();
			}else{
				this.mode = CallMode.SYNC;
			}
		}
	}

	private void initParamMetas(Method method){
		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] paramAnnos = method.getParameterAnnotations();
		Parameter[] parameters = method.getParameters();
		this.paramMetas = new ParamMeta[paramTypes.length];
		for (int idx = 0;idx < paramTypes.length; idx++){

			Class<?> type = paramTypes[idx];
			Annotation[] annos = paramAnnos[idx];
			List<Class<? extends Annotation>> annoTypes = Arrays.stream(annos).map(Annotation::annotationType).collect(Collectors.toList());
			Parameter parameter = parameters[idx];
			String name;
			String defaultValue;

			//确定ParamType
			ParamType paramType = Functions.matchAny(annoTypes,ParamType.QUERY_PARAM,
					Case.of(QueryParam.class,(t)->  ParamType.QUERY_PARAM),
					Case.of(FormParam.class,(t)-> ParamType.FORM_PARAM),
					Case.of(CookieParam.class,(t)->ParamType.COOKIE_PARAM),
					Case.of(HeaderParam.class,(t)->ParamType.HEADER_PARAM),
					Case.of(PathParam.class,(t)->ParamType.PATH_PARAM),
					Case.of(BodyParam.class,(t)-> ParamType.BODY_PARAM),
					Case.of(SessionParam.class,(t)-> ParamType.SESSION_PARAM));

			//确定参数名和defaultValue
			name = Anno.getAnnotationValue(parameter,"value",null,
					QueryParam.class,
					FormParam.class,
					CookieParam.class,
					HeaderParam.class,
					PathParam.class,
					SessionParam.class);
			defaultValue = Anno.getAnnotationValue(parameter,"value",null,DefaultValue.class);

			ParamMeta meta = new ParamMeta(name,type,defaultValue);
			meta.setParamType(paramType);
			this.paramMetas[idx] = meta;
		}
	}

	private void initPath(String rootPath,Method method){
		String subPath = method.isAnnotationPresent(Path.class) ? method.getAnnotation(Path.class).value() : Strings.EMPTY;
		rootPath = rootPath.startsWith(Strings.SLASH) ? rootPath : Strings.SLASH + rootPath;
		rootPath = rootPath.endsWith(Strings.SLASH) ? rootPath.substring(0,rootPath.length() - 1) : rootPath;
		subPath = !Strings.EMPTY.equals(subPath) && subPath.startsWith(Strings.SLASH) ? subPath.substring(1) : subPath;
		subPath = !Strings.EMPTY.equals(subPath) && subPath.endsWith(Strings.SLASH) ? subPath.substring(0,subPath.length() - 1) : subPath;
		this.path = rootPath + Strings.SLASH + subPath;
	}

	private void initProduceType(Method method){
		Class<?> returnType = method.getReturnType();
		if (returnType.isAssignableFrom(Future.class)){
			returnType =  Reflect.getClass(Reflect.getBaseType(method.getGenericReturnType()).getTypeName());
		}
		String produceType = Anno.getAnnotationValue(method,"value",null,Produces.class);
		if (produceType == null){
			//JsonObject,JsonArray,Map,List,Array => ContentType.Json
			if (returnType == JsonObject.class || returnType == JsonArray.class || Map.class.isAssignableFrom(returnType) || List.class.isAssignableFrom(returnType) || returnType.isArray()){
				produceType = ContentTypes.JSON;
			}
			//Number,String => Text
			else if (Number.class.isAssignableFrom(returnType) || String.class.isAssignableFrom(returnType)){
				produceType = ContentTypes.TEXT;
			}
			//todo modelAndView => Html
			//other should be JavaBean => Json
			else{
				produceType = ContentTypes.JSON;
			}
		}
		this.produceType = produceType;
	}


	public String getPath() {
		return path;
	}

	public ParamMeta[] getParamMetas() {
		return paramMetas;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public String getProduceType() {
		return produceType;
	}

	public io.vertx.core.http.HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public CallMode getMode() {
		return mode;
	}

	public Object getProxy() {
		return proxy;
	}

	public Method getProxyMethod() {
		return proxyMethod;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public String toString() {
		return "RouteMeta{" +
				"path='" + path + '\'' +
				", paramMetas=" + Arrays.toString(paramMetas) +
				", returnType=" + returnType +
				", produceType='" + produceType + '\'' +
				", httpMethod=" + httpMethod +
				", mode=" + mode +
				", methodName='" + methodName + '\'' +
				'}';
	}
}
