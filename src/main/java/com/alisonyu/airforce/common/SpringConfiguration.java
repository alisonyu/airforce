package com.alisonyu.airforce.common;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

/**
 * Spring的配置
 * @author yuzhiyi
 * @date 2018/10/7 20:34
 */
@Configuration(prefix = "server.spring")
public class SpringConfiguration {

	/**
	 * 是否使用Spring容器，默认不启用
	 */
	@Value("enable")
	private boolean enable = false;

	/**
	 * Spring配置类，默认使用DefaultSpringBootConfiguration
	 */
	@Value("configuration")
	private String configurationClass = DefaultSpringBootConfiguration.class.getName();


	public boolean isEnable() {
		return enable;
	}

	public String getConfigurationClass() {
		return configurationClass;
	}
}
