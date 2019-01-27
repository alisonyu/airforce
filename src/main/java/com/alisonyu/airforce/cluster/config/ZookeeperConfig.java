package com.alisonyu.airforce.cluster.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

import java.util.Arrays;

/**
 * @author yuzhiyi
 * @date 2018/9/21 10:22
 */
@Configuration(prefix = "zookeeper")
public class ZookeeperConfig {

	@Value("hosts")
 	private String[] servers;
	@Value("rootPath")
	private String namespace = "io.vertx";
	@Value("retry.initialSleepTime")
	private Integer retryIntervalSleepTime = 2000;
	@Value("retry.intervalTime")
	private Integer retryIntervalTime = 10000;
	@Value("retry.maxTimes")
	private Integer retryMaxTime = 3;
	@Value("sessionTimeout")
	private Integer sessionTimeout = 20000;
	@Value("connectTimeout")
	private Integer connectTimeout = 3000;

	public String[] getServers() {
		return servers;
	}

	public void setServers(String[] servers) {
		this.servers = servers;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Integer getRetryIntervalSleepTime() {
		return retryIntervalSleepTime;
	}

	public void setRetryIntervalSleepTime(Integer retryIntervalSleepTime) {
		this.retryIntervalSleepTime = retryIntervalSleepTime;
	}

	public Integer getRetryMaxTime() {
		return retryMaxTime;
	}

	public void setRetryMaxTime(Integer retryMaxTime) {
		this.retryMaxTime = retryMaxTime;
	}


	public Integer getRetryIntervalTime() {
		return retryIntervalTime;
	}

	public void setRetryIntervalTime(Integer retryIntervalTime) {
		this.retryIntervalTime = retryIntervalTime;
	}

	public Integer getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}


	@Override
	public String toString() {
		return "ZookeeperConfig{" +
				"servers=" + Arrays.toString(servers) +
				", namespace='" + namespace + '\'' +
				", retryIntervalSleepTime=" + retryIntervalSleepTime +
				", retryIntervalTime=" + retryIntervalTime +
				", retryMaxTime=" + retryMaxTime +
				", sessionTimeout=" + sessionTimeout +
				", connectTimeout=" + connectTimeout +
				'}';
	}
}
