package com.alisonyu.airforce.web.exception;

import com.alisonyu.airforce.web.router.RouteMeta;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理RestVerticle发生的异常
 * @author yuzhiyi
 * @date 2018/9/14 14:27
 */
public class ExceptionManager {

	private static Logger logger = LoggerFactory.getLogger(ExceptionManager.class);

	private static Map<Class<? extends Throwable>,ExceptionHandler> exceptionHandlers = new ConcurrentHashMap<>();

	static{
		//默认注册通用异常处理器
		registerExceptionHandler(new DefaultExceptionHandler());
	}

	public static Object handleException(RouteMeta meta, RoutingContext ctx,Object target,Throwable e){
		//1、获取异常类型
		Class<? extends Throwable> type = e.getClass();
		//2、根据类型获取对应的异常处理器
		ExceptionHandler handler = getExceptionHandler(type);
		//3、执行异常处理器并返回对象
		return handler.handle(meta,ctx,e);
	}

	static ExceptionHandler getExceptionHandler(Class<? extends Throwable> type){
		ExceptionHandler handler;
		if (exceptionHandlers.containsKey(type)){
			handler = exceptionHandlers.get(type);
		}else{
			handler = exceptionHandlers.get(Exception.class);
		}
		return handler;
	}


	public static void registerExceptionHandler(ExceptionHandler<? extends Object> exceptionHandler){
		exceptionHandler.conform().forEach(type -> exceptionHandlers.put((Class<? extends Throwable>) type,exceptionHandler));
	}









}
