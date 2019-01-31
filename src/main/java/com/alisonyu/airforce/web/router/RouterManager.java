package com.alisonyu.airforce.web.router;


import com.alisonyu.airforce.web.router.mounter.RouterMounter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;

/**
 * 路由管理器
 * @author yuzhiyi
 * @date 2018/10/6 21:11
 */
public class RouterManager {

	private static Router router;

	public static synchronized void init(Vertx vertx){
		router = Router.router(vertx);
	}

	public static Router getRouter(){
		return router;
	}

	public static void acceptRequest(HttpServerRequest request){
		router.handle(request);
	}

	public static void mountRouter(RouterMounter routerMounter){
		routerMounter.mount(router);
	}

}
