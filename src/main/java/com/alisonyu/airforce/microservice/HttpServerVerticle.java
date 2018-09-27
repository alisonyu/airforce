package com.alisonyu.airforce.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * @author yuzhiyi
 * @date 2018/9/12 15:05
 */
public class HttpServerVerticle extends AbstractVerticle {

	private Router router;
	private int port;

	public HttpServerVerticle(Router router,int port){
		this.router = router;
		this.port = port;
	}

	@Override
	public void start() throws Exception {

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(port);
	}

}
