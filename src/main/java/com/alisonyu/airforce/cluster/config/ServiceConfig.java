package com.alisonyu.airforce.cluster.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

/**
 * @author yuzhiyi
 * @date 2018/9/21 10:05
 */
@Configuration(prefix = "cluster.service")
public class ServiceConfig {

	@Value("name")
	private String name;
	@Value("register")
	private Boolean register = false;
	@Value("fetch-registry")
	private Boolean fetchRegistry = false;

	public String getName() {
		return name;
	}

	public Boolean getRegister() {
		return register;
	}

	public Boolean getFetchRegistry() {
		return fetchRegistry;
	}



	@Override
	public String toString() {
		return "ServiceConfig{" +
				", name='" + name + '\'' +
				", register=" + register +
				", fetchRegistry=" + fetchRegistry +
				'}';
	}
}
