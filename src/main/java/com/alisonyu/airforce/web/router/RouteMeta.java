package com.alisonyu.airforce.web.router;

import com.alisonyu.airforce.common.tool.functional.Case;
import com.alisonyu.airforce.common.tool.functional.Functions;
import com.alisonyu.airforce.ratelimiter.AirforceRateLimitConfig;
import com.alisonyu.airforce.ratelimiter.AirforceRateLimiter;
import com.alisonyu.airforce.ratelimiter.RateLimit;
import com.alisonyu.airforce.web.anno.Chunk;
import com.alisonyu.airforce.web.constant.CallMode;
import com.alisonyu.airforce.web.constant.http.ContentTypes;
import com.alisonyu.airforce.common.constant.Strings;
import com.alisonyu.airforce.web.anno.Sync;
import com.alisonyu.airforce.web.executor.param.ParamMeta;
import com.alisonyu.airforce.common.tool.instance.Anno;
import com.alisonyu.airforce.common.tool.instance.Reflect;
import com.alisonyu.airforce.web.executor.param.ParamMetaFactory;
import com.alisonyu.airforce.web.template.ModelView;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

/**
 * todo 将方法作为另外的源信息抽出来
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

	/**
	 * content-length是否为chunk
	 */
	private boolean chunk = false;

	/**
	 * 限速配置
	 */
	private AirforceRateLimitConfig rateLimiterConfig;


	public RouteMeta(String rootPath,Method method){
		this.methodName = method.getName();
		this.returnType = method.getReturnType();
		this.proxyMethod = method;
		initProduceType(method);
		initChunk(method);
		initHttpMethod(method);
		initMode(method);
		initRateLimitConfig(method);
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
						Case.of(DELETE.class,()->io.vertx.core.http.HttpMethod.DELETE),
						Case.of(OPTIONS.class,()-> io.vertx.core.http.HttpMethod.OPTIONS),
						Case.of(HEAD.class,()-> io.vertx.core.http.HttpMethod.HEAD)
						));
	}

	private void initChunk(Method method){
		this.chunk = Anno.isMark(method, Chunk.class);
	}

	private void initRateLimitConfig(Method method){
		RateLimit rateLimit = method.getAnnotation(RateLimit.class);
		if (rateLimit != null){
			int limit = rateLimit.limitForPeriod();
			long refreshPeriod = rateLimit.limitRefreshPeriod();
			long waitTime = rateLimit.timeoutDuration();
			AirforceRateLimitConfig config = new AirforceRateLimitConfig(Duration.ofMillis(waitTime),Duration.ofMillis(refreshPeriod),limit);
			this.rateLimiterConfig = config;
		}
	}

	/**
	 * 推断调用模式，一般是在EventLoop上执行操作
	 * @param method
	 */
	private void initMode(Method method){
		Annotation anno = method.getAnnotation(Sync.class);
		Class<?> returnType = method.getReturnType();
		this.mode = anno == null ? CallMode.EventLoop : CallMode.Worker;
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
		if (returnType.isAssignableFrom(ModelView.class)){
			produceType = ContentTypes.HTML;
			return;
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

	public boolean isChunk(){
		return chunk;
	}

	public AirforceRateLimitConfig getRateLimiterConfig(){
		return rateLimiterConfig;
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
				", proxy=" + proxy +
				", proxyMethod=" + proxyMethod +
				", methodName='" + methodName + '\'' +
				", chunk=" + chunk +
				'}';
	}
}
