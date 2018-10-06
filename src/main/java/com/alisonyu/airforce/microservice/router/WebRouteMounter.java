package com.alisonyu.airforce.microservice.router;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

/**
 * @author yuzhiyi
 * @date 2018/10/6 21:08
 */
public class WebRouteMounter implements RouterMounter {


	@Override
	public void mount(Router router) {
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(ResponseContentTypeHandler.create());
	}


}
