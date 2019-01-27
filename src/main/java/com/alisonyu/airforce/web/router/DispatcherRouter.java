package com.alisonyu.airforce.web.router;

import io.vertx.core.http.HttpMethod;

/**
 * @author yuzhiyi
 * @date 2018/10/7 9:14
 */
public class DispatcherRouter {

	public static String getDispatcherAddress(HttpMethod method, String url){
		return "web:airforce#"+method+processUrl(url);
	}

	private static String processUrl(String url){
		return url.replaceAll("\\/","#");
	}

}
