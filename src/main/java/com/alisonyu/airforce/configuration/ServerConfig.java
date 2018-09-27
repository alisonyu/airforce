package com.alisonyu.airforce.configuration;

import com.alisonyu.airforce.cloud.config.anno.EnableAirforceCloud;
import com.alisonyu.airforce.tool.instance.Anno;

/**
 * 服务器配置
 * @author yuzhiyi
 * @date 2018/9/17 16:29
 */
public class ServerConfig {

	private static Integer port;
	private static Boolean enableCloud;

	public static void init(Class<?> applicationClass,String...args){
		port = AirForceEnv.getConfig(AirForceDefaultConfig.SERVER_PORT,Integer.class);
		enableCloud = Anno.isMark(applicationClass, EnableAirforceCloud.class);
	}

	public static Integer getPort() {
		return port;
	}

	public static Boolean getEnableCloud() {
		return enableCloud;
	}
}
