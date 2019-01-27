package com.alisonyu.airforce.web.router;


import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * 路由管理器
 * @author yuzhiyi
 * @date 2018/10/6 21:11
 */
public class RouterManager {

	private Router router;

	public RouterManager(Vertx vertx){
		router = Router.router(vertx);
	}

	public Router getRouter(){
		return router;
	}

	public void doMount(RouterMounter routerMounter){
		routerMounter.mount(router);
	}

}
