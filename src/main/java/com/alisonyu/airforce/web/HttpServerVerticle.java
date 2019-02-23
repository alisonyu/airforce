package com.alisonyu.airforce.web;

import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.web.config.HttpServerConfig;
import com.alisonyu.airforce.web.router.RouterManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yuzhiyi
 * @date 2018/9/12 15:05
 */
public class HttpServerVerticle extends AbstractVerticle {

	private Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
	private HttpServerOptions options;

	public HttpServerVerticle(){
		this.options = new HttpServerOptions();
	}

	public HttpServerVerticle(HttpServerOptions options){
		this.options = options;
	}


	@Override
	public void start() throws Exception {
		HttpServer server = vertx.createHttpServer(this.options);
		int port = AirForceEnv.getConfig(HttpServerConfig.class).getPort();
		server.requestHandler(RouterManager::acceptRequest)
				.listen(port,as->{
					if (as.succeeded()){
						logger.info("http server listen at {}",port);
					}else{
						logger.error("start http server error",as.cause());
					}
				});
	}

}
