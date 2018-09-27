package com.alisonyu.airforce.cloud.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

import java.util.Arrays;

/**
 * @author yuzhiyi
 * @date 2018/9/21 10:22
 */
@Configuration(prefix = "cloud.zookeeper")
public class ZookeeperConfig {

	@Value("servers")
 	private String[] servers;
	@Value("namespace")
	private String namespace = "airForce";
	@Value("sessionTimeoutMs")
	private Integer sessionTimeoutMs = 60*1000;
	@Value("connectionTimeoutMs")
	private Integer connectionTimeoutMs = 15*1000;
	@Value("retry.interval")
	private Integer retryInterval = 2000;

	public String[] getServers() {
		return servers;
	}

	public Integer getSessionTimeoutMs() {
		return sessionTimeoutMs;
	}

	public Integer getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}

	public Integer getRetryInterval() {
		return retryInterval;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public String toString() {
		return "ZookeeperConfig{" +
				"servers=" + Arrays.toString(servers) +
				", namespace='" + namespace + '\'' +
				", sessionTimeoutMs=" + sessionTimeoutMs +
				", connectionTimeoutMs=" + connectionTimeoutMs +
				", retryInterval=" + retryInterval +
				'}';
	}
}
