package com.alisonyu.airforce.microservice;

import com.alisonyu.airforce.configuration.AirForceDefaultConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;

import java.io.InputStream;

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
		Integer bufferSize = AirForceEnv.getConfig(AirForceDefaultConfig.BACKPRESSURE_BUFFER_SIZE, Integer.class);
		server.requestStream()
				.toFlowable()
				.map(HttpServerRequest::pause)
				//back pressure protect app from crashing
				.onBackpressureDrop(req -> {
					req.response().setStatusCode(503).end();
				})
				.observeOn(RxHelper.scheduler(vertx.getDelegate()),false,bufferSize)
				.subscribe(req -> {
					req.resume();
					router.handle(req.getDelegate());
				});

		server.rxListen(port)
				.subscribe(res -> System.out.println("start server successfully"));

	}

}
