package com.alisonyu.airforce.microservice.core;

import com.alisonyu.airforce.constant.ParamType;
import com.alisonyu.airforce.microservice.meta.ParamMeta;
import com.alisonyu.airforce.tool.Case;
import com.alisonyu.airforce.tool.Matcher;
import com.alisonyu.airforce.tool.instance.Instance;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import java.util.function.Function;

/**
 * 接受ParamMeta和当前请求的上下文，然后返回对应元素的值
 * @author yuzhiyi
 * @date 2018/9/27 14:17
 */
class ParamAcceptor {

	/**
	 * 接受ParamMeta和当前请求的上下文，然后返回对应元素的值
	 * @param paramMeta 参数元数据
	 * @param context   请求上下文
	 * @return 参数的值
	 */
	static Object accept(ParamMeta paramMeta, RoutingContext context){
		final MultiMap queryParams = context.request().params();
		final ParamType paramType = paramMeta.getParamType();
		Object out;
		//如果参数被@BodyParam修饰,将其转为对象或者是JsonObject
		if (paramType == ParamType.BODY_PARAM){
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
		//如果参数不是@BodyParam，那么就根据不同类型获取相应的输入值。然后根据参数的实际类型获取相应的输出值
		else{
			String in = getInputValue(paramMeta, context);
			in = in == null ? paramMeta.getDefaultValue() : in;
			out = getValue(paramMeta,in);
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


	private static Object getValue(ParamMeta paramMeta,String in){
		Class<?> type = paramMeta.getType();
		return TYPE_MATCHER.match(type).apply(in);
	}

}
