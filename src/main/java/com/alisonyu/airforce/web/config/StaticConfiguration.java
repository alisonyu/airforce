package com.alisonyu.airforce.web.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;
import io.vertx.ext.web.handler.StaticHandler;

import java.nio.charset.Charset;

/**
 * 静态路由配置
 * @author yuzhiyi
 * @date 2018/10/6 21:14
 */
@Configuration(prefix = "server.static")
public class StaticConfiguration {

	@Value("root")
	private String root = "webroot";

	@Value("path")
	private String path = "/static/*";

	@Value("encoding")
	private String encoding = Charset.defaultCharset().name();

	@Value("cache.enable")
	private boolean cacheEnable = StaticHandler.DEFAULT_CACHING_ENABLED;

	@Value("cache.maxAge")
	private long maxAge = StaticHandler.DEFAULT_MAX_AGE_SECONDS;

	@Value("cache.cacheTimeout")
	private long cacheTimeout = StaticHandler.DEFAULT_CACHE_ENTRY_TIMEOUT;

	@Value("cache.maxCacheSize")
	private int maxCacheSize = StaticHandler.DEFAULT_MAX_CACHE_SIZE;



	public String getRoot() {
		return root;
	}

	public String getPath() {
		return path;
	}

	public String getEncoding() {
		return encoding;
	}

	public boolean isCacheEnable() {
		return cacheEnable;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public long getCacheTimeout() {
		return cacheTimeout;
	}

	public int getMaxCacheSize() {
		return maxCacheSize;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setCacheEnable(boolean cacheEnable) {
		this.cacheEnable = cacheEnable;
	}

	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}

	public void setCacheTimeout(long cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}

	public void setMaxCacheSize(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}

	@Override
	public String toString() {
		return "StaticConfiguration{" +
				"root='" + root + '\'' +
				", path='" + path + '\'' +
				", encoding='" + encoding + '\'' +
				", cacheEnable=" + cacheEnable +
				", maxAge=" + maxAge +
				", cacheTimeout=" + cacheTimeout +
				", maxCacheSize=" + maxCacheSize +
				'}';
	}
}
