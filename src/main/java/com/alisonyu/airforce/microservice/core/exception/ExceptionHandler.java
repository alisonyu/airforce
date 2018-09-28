package com.alisonyu.airforce.microservice.core.exception;

import com.alisonyu.airforce.microservice.meta.RouteMeta;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public interface ExceptionHandler {

	List<Class<? extends Exception>> conform();

	Object handle(RouteMeta routeMeta, RoutingContext context, Exception e);

}
