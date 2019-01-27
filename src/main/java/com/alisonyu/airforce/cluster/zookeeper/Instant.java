package com.alisonyu.airforce.cluster.zookeeper;

public interface Instant {

	String REGISTER_LOCK_PATH = "/service-register-lock";
	String PROVIDERS = "providers";
	String CONSUMERS = "consumers";
	String SERVICE = "service";
	String SUBSCRIBER = "subscriber";

}
