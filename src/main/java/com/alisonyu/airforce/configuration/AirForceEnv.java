package com.alisonyu.airforce.configuration;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;
import com.alisonyu.airforce.constant.Strings;
import com.alisonyu.airforce.tool.instance.Instance;
import com.alisonyu.airforce.tool.Pair;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 该类用于读取AirForce应用的配置文件的配置项
 * @author yuzhiyi
 * @date 2018/9/17 15:06
 */
public class AirForceEnv {


	private static final String CONFIG_FILE_NAME = "airForceConfiguration.json";
	private static JsonObject config = new JsonObject();

	/**
	 * 框架内部使用，用户不应该主动调用该方法
	 */
	public static void init(Vertx vertx){
		if(vertx.fileSystem().existsBlocking(CONFIG_FILE_NAME)){
			config = vertx.fileSystem().readFileBlocking(CONFIG_FILE_NAME).toJsonObject();
		}else{
			config = new JsonObject();
		}
	}

	/**
	 * 例如获取server.port 可以使用 AirForceEnv.get(server.Integer.class,9000)
	 */
	public static <T> T getConfig(String expr,Class<T> type,T defaultValue){
		if (expr == null || "".equals(expr)){
			return null;
		}
		Object in = getConfigValue(expr);
		in = in != null? in : defaultValue;
		return Instance.cast(in,type);
	}

	private static Object getConfig(String expr, Type type,Class<?> typeClazz,Object defaultValue){
		if (expr == null || "".equals(expr)){
			return null;
		}
		Object in = getConfigValue(expr);
		in = in != null? in : defaultValue;
		return Instance.cast(in,type,typeClazz);
	}





	private static Object getConfigValue(String expr){
		if (expr == null || "".equals(expr)){
			return null;
		}
		String[] in = expr.split("\\.");
		if (in.length == 1){
			return in[0];
		}
		else{
			JsonObject node = config;
			for (int i = 0;i<in.length-1;i++){
				node = node.getJsonObject(in[i]);
				if (node == null) {
					return null;
				}
			}
			return node.getValue(in[in.length-1]);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getConfig(Class<T> type){
		return (T) ConfigPool.configPool.computeIfAbsent(type, AirForceEnv::injectConfigInstance);
	}


	public static <V> V getConfig(Pair<String,V> pair,Class<V> type){
		return getConfig(pair.getKey(),type,pair.getValue());
	}

	private static <T> T injectConfigInstance(Class<T> type){
		T instance = Instance.instance(type);
		String prefix = type.isAnnotationPresent(Configuration.class) ? type.getAnnotation(Configuration.class).prefix() : Strings.EMPTY;
		Arrays.stream(type.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Value.class))
				.forEach(field -> {
					String key = prefix + Strings.DOT + field.getAnnotation(Value.class).value();
					Object in = getConfig(key,field.getGenericType(),field.getType(),null);
					if (in != null){
						Instance.jvmSet(instance,field.getName(),in);
					}
				});
		return instance;
	}


}
