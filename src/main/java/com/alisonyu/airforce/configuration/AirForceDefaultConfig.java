package com.alisonyu.airforce.configuration;

import com.alisonyu.airforce.tool.Pair;

/**
 * AirForce框架默认配置
 * @author yuzhiyi
 * @date 2018/9/17 15:40
 */
public interface AirForceDefaultConfig {

	Pair<String,Integer> SERVER_PORT = Pair.of("server.port",9090);


}
