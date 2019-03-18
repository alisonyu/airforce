package com.alisonyu.airforce.web.router;

import com.alisonyu.airforce.core.AirForceVerticle;
import com.alisonyu.airforce.web.router.mounter.RouterMounter;
import com.alisonyu.airforce.web.transfer.UnsafeLocalMessageCodec;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * route handler will be dispatched to eventbus
 * @author yuzhiyi
 * @date 2018/10/7 9:14
 */
public class DispatcherRouter {

	public static final Integer DISPATCHER_INIT_ORDER = 1000000;

	private static Logger logger = LoggerFactory.getLogger(DispatcherRouter.class);

	private static ConcurrentHashMap<Class<?>,Boolean> airforceClassSet = new ConcurrentHashMap<>();

	private static AtomicInteger dispatcherOrders = new AtomicInteger(DISPATCHER_INIT_ORDER);

	private static final DeliveryOptions DELIVERY_OPTIONS = new DeliveryOptions().setCodecName(UnsafeLocalMessageCodec.class.getName()).setLocalOnly(true);

	/**
	 * 每一个类继承了AirforceVerticle都会注册路由,一个类只注册一次，不重复注册
	 * @param clazz
	 * @param eventBus
	 */
	public static void mountRouter(Class<? extends AirForceVerticle> clazz,EventBus eventBus){
		airforceClassSet.computeIfAbsent(clazz,key -> {
					//将在RestVerticle定义的方法转化为RouteMeta
					List<RouteMeta> routeMetas = RouteMetaManager.getRouteMetas(clazz);
					//debug routeMeta
					routeMetas.forEach(routeMeta -> logger.info(routeMeta.toString()));
					//绑定路由,派发到EventBus中执行
					routeMetas.forEach(routeMeta -> {
						RouterManager.mountRouter(new RouterMounter() {
							@Override
							public void mount(Router router) {
								router.route(routeMeta.getHttpMethod(), routeMeta.getPath())
										.order(dispatcherOrders.incrementAndGet())
										.handler(ctx -> dispatchToEventBus(eventBus, routeMeta, ctx));
							}

						});
					});
					return true;
			}
		);
	}

	private static void dispatchToEventBus(EventBus eventBus, RouteMeta routeMeta, RoutingContext context){
		String url = DispatcherRouter.getDispatcherAddress(routeMeta.getHttpMethod(),routeMeta.getPath());
		eventBus.send(url,context,DELIVERY_OPTIONS);
	}


	public static String getDispatcherAddress(HttpMethod method, String url){
		return "web:airforce#"+method+processUrl(url);
	}

	private static String processUrl(String url){
		return url.replaceAll("\\/","#");
	}


}
