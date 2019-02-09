package com.alisonyu.airforce.web;

import com.alisonyu.airforce.configuration.AirForceDefaultConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.web.config.HttpServerConfig;
import com.alisonyu.airforce.web.router.RouterManager;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DefaultValue;
import java.io.InputStream;

/**
 * @author yuzhiyi
 * @date 2018/9/12 15:05
 */
public class HttpServerVerticle extends AbstractVerticle {

	private Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

	private Router router;
	private int port;

	@Deprecated
	public HttpServerVerticle(Router router,int port){
		this.router = router;
		this.port = port;
	}

	public HttpServerVerticle(){
	}

	@Override
	public void start() throws Exception {
		HttpServer server = vertx.createHttpServer();
		int port = AirForceEnv.getConfig(HttpServerConfig.class).getPort();
		server.getDelegate()
				.requestHandler(req -> {
					RouterManager.acceptRequest(req);
				});

		server.listen(port);
		logger.info("http server listen at {}",port);
	}

}
