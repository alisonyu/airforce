package com.alisonyu.airforce;

import com.alisonyu.airforce.cloud.config.ServiceConfig;
import com.alisonyu.airforce.cloud.core.ServiceRecord;
import com.alisonyu.airforce.common.Container;
import com.alisonyu.airforce.common.ContainerFactory;
import com.alisonyu.airforce.configuration.AirForceDefaultConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.configuration.ServerConfig;
import com.alisonyu.airforce.constant.Banner;
import com.alisonyu.airforce.microservice.AbstractRestVerticle;
import com.alisonyu.airforce.microservice.HttpServerVerticle;
import com.alisonyu.airforce.microservice.router.RouterManager;
import com.alisonyu.airforce.microservice.router.StaticConfiguration;
import com.alisonyu.airforce.microservice.router.StaticRouteMounter;
import com.alisonyu.airforce.microservice.router.WebRouteMounter;
import com.alisonyu.airforce.tool.Network;
import com.alisonyu.airforce.tool.TimeMeter;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.StaticHandler;
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
		//初始化Vertx
		Vertx vertx = instanceVertx();
		//初始化应用配置
		initConfig(vertx,clazz,args);
		//configuration router
		RouterManager routerManager = initRouter(vertx);
		//deploy all restVerticle
		deployRestVerticle(vertx,routerManager);
		//if all restVerticle deployed successfully! begin listen;
		startHttpServer(vertx,routerManager.getRouter());

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

	private static RouterManager initRouter(Vertx vertx){
		RouterManager routerManager = new RouterManager(vertx);
		//1、进行Web基本挂载
		routerManager.doMount(new WebRouteMounter());
		//2、进行静态路由的挂载
		StaticConfiguration staticConfiguration = AirForceEnv.getConfig(StaticConfiguration.class);
		routerManager.doMount(new StaticRouteMounter(staticConfiguration));
		//3、todo 在Container中获取其他挂载对象进行挂载
		return routerManager;
	}

	private static void deployRestVerticle(Vertx vertx,RouterManager routerManager){
		//get all restVerticle class
		Container container = ContainerFactory.getContainer();
		Set<Class<? extends AbstractRestVerticle>> restVerticleClasses = container.getClassesImpl(AbstractRestVerticle.class);
		CountDownLatch latch = new CountDownLatch(restVerticleClasses.size());
		//deploy AbstractRestVerticle
		restVerticleClasses
				.forEach(c->{
					try {
						AbstractRestVerticle verticle = c.newInstance();
						vertx.deployVerticle(verticle,rs->{
							if (rs.succeeded()){
								routerManager.doMount(verticle);
							}
						});
					} catch (InstantiationException | IllegalAccessException e) {
						logger.error("部署{}出现异常",c.getName());
						e.printStackTrace();
					}finally {
						latch.countDown();
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
