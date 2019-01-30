package com.alisonyu.airforce.web.executor.param;

import com.alisonyu.airforce.web.constant.ParamType;
import com.alisonyu.airforce.common.tool.functional.Case;
import com.alisonyu.airforce.common.tool.functional.Matcher;
import com.alisonyu.airforce.common.tool.instance.Instance;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import java.util.function.Function;

/**
 * 接受ParamMeta和当前请求的上下文，然后返回对应元素的值
 * @author yuzhiyi
 * @date 2018/9/27 14:17
 */
public class ParamAcceptor {

	/**
	 * 接受ParamMeta和当前请求的上下文，然后返回对应元素的值
	 * @param paramMeta 参数元数据
	 * @param context   请求上下文
	 * @return 参数的值
	 */
	public static Object accept(ParamMeta paramMeta, RoutingContext context){
		final MultiMap queryParams = context.request().params();
		final ParamType paramType = paramMeta.getParamType();
		final Class<?> paramJavaType = paramMeta.getType();
		Object out;
		//判断是否是需要注入的对象
		if (isInjectObject(paramJavaType)){
			out = injectObject(paramJavaType,context);
		}
		//否则从参数中取，然后转化成对应的类型
		else{
			Object in;
			//如果参数被@BodyParam修饰,将其转为对象或者是JsonObject
			if (paramType == ParamType.BODY_PARAM){
				in = queryParams;
			}
			//如果参数不是@BodyParam，那么就根据不同类型获取相应的输入值
			else{
				in = getInputValue(paramMeta, context);
				//检验参数是否存在
				if (in == null && paramMeta.isRequired()){
					throw new IllegalArgumentException(paramMeta.getName() + "is required");
				}
				in = in == null ? paramMeta.getDefaultValue() : in;
			}
			out = getValue(paramMeta,in);
		}
		return out;
	}

	/**
	 * 根据参数元数据和输入值 来 输出结果值
	 * @param paramMeta 参数元数据
	 * @param in 输入值
	 * @return 结果值
	 */
	public static Object getValue(ParamMeta paramMeta, Object in){
		Object out;
		final ParamType paramType = paramMeta.getParamType();
		//1、处理空输入,让他等于默认值
		if (in == null){
			in = paramMeta.getDefaultValue();
		}
		//2、如果经过处理后的输入值仍为null，我们返回空即可
		if (in == null){
			out = null;
		}
		//如果参数类型是被@BodyParam修饰的，根据他的类型封装成一个JsonObject或者JavaBean
		else if (paramType == ParamType.BODY_PARAM){
			if (! (in instanceof MultiMap)){
				throw new IllegalArgumentException("@BodyParam应该要接受MultiMap参数");
			}
			MultiMap queryParams = (MultiMap)in;
			final Class<?> clazz = paramMeta.getType();
			if (String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)){
				throw new IllegalArgumentException("被@BodyParam修饰的参数类型只能是JsonObject或者是Java Bean类型");
			}
			if (clazz == JsonObject.class){
				out = multiMapToJson(queryParams);
			}
			else{
				out = castToJavaBean(clazz,queryParams);
			}
		}
		else{
			if (!(in instanceof String)){
				//为了容错性，我们将他先转化为字符串
				in = String.valueOf(in);
			}
			out = getBasicValue(paramMeta,(String)in);
		}
		return out;
	}


	private static JsonObject multiMapToJson(MultiMap map){
		JsonObject jsonObject = new JsonObject();
		map.forEach(entry -> jsonObject.put(entry.getKey(),entry.getValue()));
		return jsonObject;
	}


	private static Object castToJavaBean(Class<?> clazz,MultiMap queryParams){
		Object o = Instance.instance(clazz);
		queryParams.forEach(entry-> Instance.enhanceSet(o,entry.getKey(),entry.getValue()));
		return o;
	}


	/**
	 * 获取参数的输入值
	 */
	private static String getInputValue(ParamMeta paramMeta,RoutingContext context){
		final MultiMap queryParams = context.request().params();
		final Session session = context.session();
		final String key = paramMeta.getName();
		final ParamType paramType = paramMeta.getParamType();
		if (paramType == ParamType.BODY_PARAM){
			throw new IllegalArgumentException("该方法只处理非BodyParam的参数");
		}
		//参数输入值
		String in = null;
		//根据参数的类型获取参数的输入值
		switch (paramType){
			case QUERY_PARAM: in = queryParams.get(key); break;
			case FORM_PARAM: in = queryParams.get(key); break;
			case PATH_PARAM: in = queryParams.get(key); break;
			case HEADER_PARAM: in = context.request().getHeader(key); break;
			case COOKIE_PARAM: in = context.getCookie(key) == null ? null : context.getCookie(key).getValue() ; break;
			case SESSION_PARAM: in = session != null ? session.get(key) : null; break;
			default: break;
		}
		return in;
	}

	/**
	 * 通过匹配类型，获取对应处理输入值的Function
	 */
	private static final Matcher<Class, Function<String,Object>> TYPE_MATCHER = Matcher.of(Class.class,
										Case.of(String.class,()-> String::valueOf),
										Case.of(int.class,()->Integer::valueOf),
										Case.of(Integer.class,()-> Integer::valueOf),
										Case.of(double.class,()->Double::valueOf),
										Case.of(Double.class,()->Double::valueOf),
										Case.of(float.class,()->Float::valueOf),
										Case.of(Float.class,()->Float::valueOf),
										Case.widecard(()->String::valueOf));


	private static Object getBasicValue(ParamMeta paramMeta, String in){
		Class<?> type = paramMeta.getType();
		return TYPE_MATCHER.match(type).apply(in);
	}


	private static final Class<?>[] INJECT_TYPE = {RoutingContext.class, HttpServerRequest.class, HttpServerResponse.class,Session.class};
	private static boolean isInjectObject(Class<?> clazz){
		boolean out = false;
		for (Class<?> type: INJECT_TYPE){
			if (type == clazz){
				out = true;
				break;
			}
		}
		return out;
	}


	private static Object injectObject(Class<?> clazz,RoutingContext context){
		Object out;
		if (clazz == RoutingContext.class){
			out = context;
		}
		else if (clazz == HttpServerRequest.class){
			out = context.request();
		}
		else if (clazz == HttpServerResponse.class){
			out = context.response();
		}
		else if (clazz == Session.class){
			out = context.session();
		}
		else{
			throw new IllegalArgumentException(clazz.getName() + "不能自动注入");
		}
		return out;
	}



}
