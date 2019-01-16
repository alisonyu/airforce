package com.alisonyu.airforce.configuration;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;
import com.alisonyu.airforce.constant.Strings;
import com.alisonyu.airforce.tool.instance.Instance;
import com.alisonyu.airforce.tool.Pair;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Properties;

/**
 * 该类用于读取AirForce应用的配置文件的配置项
 * @author yuzhiyi
 * @date 2018/9/17 15:06
 */
public class AirForceEnv {

	private static Logger logger = LoggerFactory.getLogger(AirForceEnv.class);
	private static final String DEFAULT_CONFIG_PROPERTIES = "airforce.properties";
	private static final String CONFIG_FILE_NAME = "airforce.json";
	private static Config config;

	/**
	 * 框架内部使用，用户不应该主动调用该方法
	 */
	public static void init(Vertx vertx,String configPath){
		if (configPath!=null){
			String type = configPath.substring(configPath.lastIndexOf("."));
			if ("properties".equals(type)){
				config = getPropertiesConfig(vertx,configPath);
			}else if ("json".equals(type)){
				config = getJsonConfig(vertx,configPath);
			}else{
				logger.error("unknown config type");
				throw new RuntimeException("unknow config typee");
			}
			if (config == null){
				throw new RuntimeException("config not found,path:"+configPath);
			}
		}
		else{
			if (vertx.fileSystem().existsBlocking(DEFAULT_CONFIG_PROPERTIES)){
				config = getPropertiesConfig(vertx,DEFAULT_CONFIG_PROPERTIES);
			}
			else if (vertx.fileSystem().existsBlocking(CONFIG_FILE_NAME)){
				config = getJsonConfig(vertx,CONFIG_FILE_NAME);
			}
			if (config == null){
				config = new EmptyConfig();
			}
		}
	}


	private static Config getPropertiesConfig(Vertx vertx,String path){
		ClassLoader classLoader = vertx.getClass().getClassLoader();
		if (vertx.fileSystem().existsBlocking(path)){
			Properties properties = new Properties();
			InputStream in  = classLoader.getResourceAsStream("airforce.properties");
			try {
				properties.load(in);
				return new PropertiesConfig(properties);
			} catch (IOException e) {
				logger.error("init config error",e);
				throw new RuntimeException(e);
			}
		}else{
			return null;
		}
	}

	private static Config getJsonConfig(Vertx vertx,String path){
		if(vertx.fileSystem().existsBlocking(CONFIG_FILE_NAME)){
			JsonObject jsonConfig  = vertx.fileSystem().readFileBlocking(CONFIG_FILE_NAME).toJsonObject();
			return new JSONConfig(jsonConfig);
		}else{
			return null;
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

	private static String getConfigValue(String expr){
		return config.getValue(expr);
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
