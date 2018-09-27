package com.alisonyu.airforce.cloud.core;

import com.alisonyu.airforce.configuration.ServerConfig;
import com.alisonyu.airforce.tool.Network;

import java.net.InetAddress;
import java.util.Objects;

/**
 * 服务发现信息
 * @author yuzhiyi
 * @date 2018/9/21 14:57
 */
public class ServiceRecord {

	protected String host;
	protected Integer port;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public static ServiceRecord local(){
		ServiceRecord serviceRecord = new ServiceRecord();
		InetAddress address = Network.getLocalHostLANAddress();
		String host = address != null ? address.getHostAddress() : "127.0.0.1";
		serviceRecord.setHost(host);
		serviceRecord.setPort(ServerConfig.getPort());
		return serviceRecord;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ServiceRecord that = (ServiceRecord) object;
		return Objects.equals(host, that.host) &&
				Objects.equals(port, that.port);
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port);
	}

	@Override
	public String toString() {
		return "ServiceRecord{" +
				"host='" + host + '\'' +
				", port=" + port +
				'}';
	}
}
