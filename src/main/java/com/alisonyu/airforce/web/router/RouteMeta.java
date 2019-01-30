package com.alisonyu.airforce.web.router;

import com.alisonyu.airforce.common.tool.functional.Case;
import com.alisonyu.airforce.common.tool.functional.Functions;
import com.alisonyu.airforce.web.constant.CallMode;
import com.alisonyu.airforce.web.constant.http.ContentTypes;
import com.alisonyu.airforce.web.constant.ParamType;
import com.alisonyu.airforce.common.constant.Strings;
import com.alisonyu.airforce.web.anno.BodyParam;
import com.alisonyu.airforce.web.anno.SessionParam;
import com.alisonyu.airforce.web.anno.Sync;
import com.alisonyu.airforce.web.executor.param.ParamMeta;
import com.alisonyu.airforce.common.tool.instance.Anno;
import com.alisonyu.airforce.common.tool.instance.Reflect;
import com.alisonyu.airforce.web.executor.param.ParamMetaFactory;
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

	private static Logger logger = LoggerFactory.getLogger(RouteMeta.class);

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
				// todo 与HttpMethod一一对应
				.ifPresent(anno-> this.httpMethod = Functions.match(anno,
						Case.of(GET.class,()-> io.vertx.core.http.HttpMethod.GET ),
						Case.of(POST.class,()->io.vertx.core.http.HttpMethod.POST),
						Case.of(PUT.class,()-> io.vertx.core.http.HttpMethod.PUT),
						Case.of(DELETE.class,()->io.vertx.core.http.HttpMethod.DELETE)));
	}

	/**
	 * 推断调用模式，一般是使用异步ASYNC调用模式
	 * @param method
	 */
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
		this.paramMetas = ParamMetaFactory.getParamMeta(method);
	}

	/**
	 * Rest路由路径
	 */
	private void initPath(String rootPath,Method method){
		String subPath = method.isAnnotationPresent(Path.class) ? method.getAnnotation(Path.class).value() : Strings.EMPTY;
		rootPath = rootPath.startsWith(Strings.SLASH) ? rootPath : Strings.SLASH + rootPath;
		rootPath = rootPath.endsWith(Strings.SLASH) ? rootPath.substring(0,rootPath.length() - 1) : rootPath;
		subPath = !Strings.EMPTY.equals(subPath) && subPath.startsWith(Strings.SLASH) ? subPath.substring(1) : subPath;
		subPath = !Strings.EMPTY.equals(subPath) && subPath.endsWith(Strings.SLASH) ? subPath.substring(0,subPath.length() - 1) : subPath;
		this.path = rootPath + Strings.SLASH + subPath;
	}

	/**
	 * 根据返回类型推断content-type
	 * @param method
	 */
	private void initProduceType(Method method){
		Class<?> returnType = method.getReturnType();
		if (returnType.isAssignableFrom(Future.class) || returnType.isAssignableFrom(Flowable.class)){
			returnType =  Reflect.getClass(Reflect.getBaseType(method.getGenericReturnType()).getTypeName());
		}
		String[] produceTypes = (String[])Anno.getAnnotationValue(method,"value",null,Produces.class);
		this.produceType = (produceTypes == null || (produceTypes.length > 0 && produceTypes[0] == null)) ? null :  produceTypes[0];
		if (produceType == null){
			//JsonObject,JsonArray,Map,List,Array => ContentType.Json
			if (returnType == JsonObject.class || returnType == JsonArray.class || Map.class.isAssignableFrom(returnType) || List.class.isAssignableFrom(returnType) || returnType.isArray()){
				produceType = ContentTypes.JSON;
			}
			//Number,String => Text
			else if (Number.class.isAssignableFrom(returnType) || String.class.isAssignableFrom(returnType)){
				produceType = ContentTypes.TEXT;
			}
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
