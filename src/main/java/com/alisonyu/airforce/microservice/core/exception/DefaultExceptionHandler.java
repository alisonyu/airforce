package com.alisonyu.airforce.microservice.core.exception;

import com.alisonyu.airforce.microservice.meta.RouteMeta;
import com.google.common.collect.Lists;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * 默认的异常处理器
 * @author yuzhiyi
 * @date 2018/9/28 21:29
 */
public class DefaultExceptionHandler implements ExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	@Override
	public List<Class<? extends Exception>> conform() {
		return Collections.singletonList(Exception.class);
	}

	@Override
	public Object handle(RouteMeta routeMeta, RoutingContext context, Exception e) {
		//记录log
		logger.error("{} occur error: {}",routeMeta.getPath(),e.getMessage());
		//设置500返回码
		context.response().setStatusCode(HttpExceptionCode.INTERNAL_SERVER_ERROR.value);
		//打印堆栈信息来排除bug
		e.printStackTrace();
		//返回ErrorMessage对象
		ErrorMessage message = new ErrorMessage();
		message.setPath(routeMeta.getPath());
		message.setMethod(routeMeta.getHttpMethod().name());
		message.setTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		message.setHttpCode(HttpExceptionCode.INTERNAL_SERVER_ERROR.value);
		message.setErrorMessage(e.getMessage());
		return message;
	}



}
