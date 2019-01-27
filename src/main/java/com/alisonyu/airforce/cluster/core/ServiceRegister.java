package com.alisonyu.airforce.cluster.core;

/**
 * 服务注册接口
 */
public interface ServiceRegister {

	/**
	 * 创建服务
	 */
	void createService(String serviceName);

	/**
	 * 注册服务，主要是将自身的服务注册上去
	 */
	void registerService(String serviceName,ServiceRecord record);

	/**
	 * 注销服务，将自身的服务注销
	 */
	void unregisterService(String serviceName,ServiceRecord record);

	/**
	 * 删除服务
	 */
	void deleteService(String serviceName);


}
