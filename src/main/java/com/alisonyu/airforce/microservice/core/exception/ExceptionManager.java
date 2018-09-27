package com.alisonyu.airforce.microservice.core.exception;

import com.alisonyu.airforce.microservice.meta.RouteMeta;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 处理RestVerticle发生的异常
 * 1、允许用户自定义异常处理器
 * 2、发生异常的时候，有一个默认的异常处理器进行处理，该处理器进行logger操作,并且返回500错误
 * @author yuzhiyi
 * @date 2018/9/14 14:27
 */
public class ExceptionManager {

	private static Logger logger = LoggerFactory.getLogger(ExceptionManager.class);

	private static Map<Class<? extends Exception>,Function<? extends Exception,Object>> exceptionHandlers = new ConcurrentHashMap<>();


	public static void handleException(RouteMeta meta, RoutingContext ctx,Object target,Exception e){
		HttpServerResponse resp = ctx.response();
		JsonObject json = new JsonObject()
							.put("error","server meet some trouble!");
		resp.setStatusCode(500);
		resp.end(json.toString());
		logger.error("exception message : {}", e.getMessage());
		logger.error("exception cause : {}", e.getCause());
		logger.error("exception suppressed : {}", Arrays.toString(e.getSuppressed()));
		//异常输出
		logger.error("exception toString and track space : {}", "\r\n" + e);
		logger.error(errorTrackSpace(e));
		logger.error("---------------------------------------------");
		e.printStackTrace();
	}




	private static String errorTrackSpace(Exception e) {
		StringBuffer sb = new StringBuffer();
		if (e != null) {
			for (StackTraceElement element : e.getStackTrace()) {
				sb.append("\r\n\t").append(element);
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}




}
