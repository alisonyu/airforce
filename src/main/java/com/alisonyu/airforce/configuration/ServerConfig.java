package com.alisonyu.airforce.configuration;

import com.alisonyu.airforce.cluster.config.anno.EnableAirforceCloud;
import com.alisonyu.airforce.common.tool.instance.Anno;

/**
 * todo 重构
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
