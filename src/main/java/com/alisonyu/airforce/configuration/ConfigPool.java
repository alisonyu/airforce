package com.alisonyu.airforce.configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置缓存
 */
interface ConfigPool {

	ConcurrentHashMap<Class<?>,Object> configPool = new ConcurrentHashMap<>();

}
