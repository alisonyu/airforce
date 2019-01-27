package com.alisonyu.airforce.cluster.core;

import java.util.List;

public interface ServiceDiscovery {

	List<? extends ServiceRecord> getService(String serviceName);

	void subscribeService(String serviceName);

	void unsubscribeService(String serviceName);

}
