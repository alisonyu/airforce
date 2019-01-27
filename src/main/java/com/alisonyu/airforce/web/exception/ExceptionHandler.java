package com.alisonyu.airforce.web.exception;

import com.alisonyu.airforce.web.router.RouteMeta;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public interface ExceptionHandler {

	/**
	 * 表示该异常处理器将处理哪些异常类
	 */
	List<Class<? extends Throwable>> conform();

	/**
	 * 处理异常的具体逻辑
	 * @param routeMeta 发生异常的路由
	 * @param context   web上下文
	 * @param e			具体异常
	 * @return			返回一个具体的值，该值将序列化返回给请求端
	 */
	Object handle(RouteMeta routeMeta, RoutingContext context, Throwable e);

}
