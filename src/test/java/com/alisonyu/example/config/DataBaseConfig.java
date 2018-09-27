package com.alisonyu.example.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

/**
 * @author yuzhiyi
 * @date 2018/9/17 19:01
 */
@Configuration(prefix = "database")
public class DataBaseConfig {

	@Value("driver")
	private String driver;
	@Value("url")
	private String url;
	@Value("port")
	private Integer port;
	@Value("username")
	private String username;
	@Value("password")
	private String password;

	public String getDriver() {
		return driver;
	}

	public String getUrl() {
		return url;
	}

	public Integer getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "DataBaseConfig{" +
				"driver='" + driver + '\'' +
				", url='" + url + '\'' +
				", port=" + port +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
