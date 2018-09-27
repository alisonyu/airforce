package com.alisonyu.airforce;

import com.alisonyu.airforce.cloud.config.ServiceConfig;
import com.alisonyu.airforce.cloud.core.ServiceRecord;
import com.alisonyu.airforce.configuration.AirForceDefaultConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.configuration.ServerConfig;
import com.alisonyu.airforce.constant.Banner;
import com.alisonyu.airforce.microservice.HttpServerVerticle;
import com.alisonyu.airforce.microservice.RestVerticle;
import com.alisonyu.airforce.tool.Network;
import com.alisonyu.airforce.tool.TimeMeter;
import com.alisonyu.airforce.tool.instance.ScanedClass;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author yuzhiyi
 * @date 2018/9/14 10:37
 */
public class AirForceApplication {

	private static Logger logger = LoggerFactory.getLogger(AirForceApplication.class);

	public static void run(Class clazz ,String... args){
		showBanner();
		//包扫描,并将类结果缓存
		scanClasses();
		//初始化Vertx
		Vertx vertx = instanceVertx();
		//初始化应用配置
		initConfig(vertx,clazz,args);
		//configuration router
		Router router = initRouter(vertx);
		//deploy all restVerticle
		deployRestVerticle(vertx,router);
		//if all restVerticle deployed successfully! begin listen;
		startHttpServer(vertx,router);
		//if cloud is enabled,start service register and discovery
		//startCloudApplication();
	}

	private static void showBanner(){
		System.out.println(Banner.defaultBanner);
	}

	private static void initConfig(Vertx vertx,Class<?> applicationClass,String ... args){
		AirForceEnv.init(vertx);
		ServerConfig.init(applicationClass, args);
		logger.debug("读取配置信息成功");
	}

	private static Vertx instanceVertx(){
		TimeMeter timeMeter = new TimeMeter();
		timeMeter.start();
		try{
			return Vertx.vertx();
		}finally {
			logger.debug("实例化Vertx使用了{}ms",timeMeter.end());
		}
	}

	private static void scanClasses(){
		TimeMeter timeMeter = new TimeMeter();
		timeMeter.start();
		ScanedClass.scan();
		logger.debug("类扫描使用了{}ms",timeMeter.end());
	}

	private static Router initRouter(Vertx vertx){
		Router router = Router.router(vertx);
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(ResponseContentTypeHandler.create());
		return router;
	}

	private static void deployRestVerticle(Vertx vertx,Router router){
		//get all restVerticle class
		Set<Class<?>> restVerticleClasses = ScanedClass.
				getClasses(cls -> RestVerticle.class.isAssignableFrom(cls) && cls != RestVerticle.class);
		CountDownLatch latch = new CountDownLatch(restVerticleClasses.size());
		//deploy RestVerticle
		restVerticleClasses
				.forEach(c->{
					try {
						RestVerticle verticle = (RestVerticle) c.newInstance();
						vertx.deployVerticle(verticle,rs->{
							if (rs.succeeded()){
								//部署成功就开始挂载路由
								verticle.mount(router);
								latch.countDown();
							}
						});
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				});
		//if all restVerticle deployed successfully! begin listen;
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void startHttpServer(Vertx vertx,Router router){
		TimeMeter timeMeter = new TimeMeter();
		timeMeter.start();
		Integer port = AirForceEnv.getConfig(AirForceDefaultConfig.SERVER_PORT,Integer.class);
		HttpServerVerticle httpServerVerticle = new HttpServerVerticle(router,port);
		CountDownLatch latch = new CountDownLatch(1);
		vertx.deployVerticle(httpServerVerticle,as->{
			latch.countDown();
			if (as.succeeded()){
				logger.info("server listen at port: {}! cost {}ms",port,timeMeter.end());
			}else{
				logger.error(as.cause().getMessage());
				as.cause().printStackTrace();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void startCloudApplication(){
		if (ServerConfig.getEnableCloud()){
			ServiceConfig serviceConfig = AirForceEnv.getConfig(ServiceConfig.class);
			String serviceName = serviceConfig.getName();
			ServiceRecord serviceRecord = new ServiceRecord();
			InetAddress address = Network.getLocalHostLANAddress();
			String host = address != null ? address.getHostAddress() : "127.0.0.1";
			serviceRecord.setHost(host);
			serviceRecord.setPort(ServerConfig.getPort());
			//ZooKeeperServiceRegister register = ZooKeeperServiceRegister.instance();
			//register.registerService(serviceName,serviceRecord);
		}
	}


}
