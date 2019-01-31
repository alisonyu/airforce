package com.alisonyu.airforce.web.router.mounter;

import com.alisonyu.airforce.web.config.StaticConfiguration;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * 静态文件路由处理器
 * @author yuzhiyi
 * @date 2018/10/6 21:30
 */
public class StaticRouteMounter implements RouterMounter{

	private StaticConfiguration configuration;

	public StaticRouteMounter(StaticConfiguration configuration){
		this.configuration = configuration;
	}


	@Override
	public void mount(Router router) {
		StaticHandler staticHandler = StaticHandler.create();
		staticHandler.setWebRoot(configuration.getRoot());
		staticHandler.setDefaultContentEncoding(configuration.getEncoding());
		staticHandler.setCachingEnabled(configuration.isCacheEnable());
		staticHandler.setMaxAgeSeconds(configuration.getMaxAge());
		staticHandler.setMaxCacheSize(configuration.getMaxCacheSize());
		staticHandler.setCacheEntryTimeout(configuration.getCacheTimeout());
		final String path = configuration.getPath();
		router.route(path).handler(staticHandler);
	}

}
