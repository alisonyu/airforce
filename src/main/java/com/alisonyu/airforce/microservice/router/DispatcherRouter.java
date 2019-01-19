package com.alisonyu.airforce.microservice.router;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;

/**
 * @author yuzhiyi
 * @date 2018/10/7 9:14
 */
public class DispatcherRouter implements RouterMounter {

	private final EventBus eventBus;
	private final DeliveryOptions options;

	public DispatcherRouter(EventBus eventBus){
		this.eventBus = eventBus;
		this.options = new DeliveryOptions();
		MessageCodec messageCodec = new UnsafeLocalMessageCodec();
		eventBus.registerCodec(messageCodec);
		options.setCodecName(messageCodec.name());
	}

	@Override
	public void mount(Router router) {
		router.route().handler(ctx->{
			//todo 在发送之前检查该URL是否注册了
			//todo 如果没有，使用算法进行匹配Path路由



		});
	}

	public static String getDispathcerAddress(HttpMethod method, String url){
		return "web:airforce#"+method+processUrl(url);
	}

	private static String processUrl(String url){
		return url.replaceAll("\\/","#");
	}







}
