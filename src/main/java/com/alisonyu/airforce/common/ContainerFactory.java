package com.alisonyu.airforce.common;

/**
 * 容器工厂
 * @author yuzhiyi
 * @date 2018/10/6 20:10
 */
public class ContainerFactory {

	private static Container container;

	public static Container getContainer(){
		if (container == null){
			synchronized (ContainerFactory.class){
				if (container == null){
					container = new AirForceContainer();
				}
			}
		}
		return container;
	}

	/**
	 * 注册容器，当容器有其他实现的时候通过该接口进行注册
	 * 默认的容器实现是com.alisonyu.airforce.common.AirForceContainer
	 * @param container 要注册的容器
	 */
	public synchronized static void registerContainer(Container container){
		ContainerFactory.container = container;
	}


}
