package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.airforce.microservice.service.provider.ServiceProvider;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ConsumerProvider {

    public static <T> T getConsumer(Vertx vertx, Class<T> itf, String group, String version, CircuitBreakerOptions circuitBreakerOptions){
        InvocationHandler invocationHandler = new ConsumeInvocationHandler(vertx,itf,group,version,circuitBreakerOptions,null);
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[]{itf},invocationHandler);
    }

    public static <T> T getConsumer(Vertx vertx,Class<T> itf){
        return getConsumer(vertx,itf, ServiceProvider.defaultGroup,ServiceProvider.defaultVersion,null);
    }

    public static <T> T getConsumer(Vertx vertx,Class<T> itf,CircuitBreakerOptions circuitBreakerOptions){
        return getConsumer(vertx,itf, ServiceProvider.defaultGroup,ServiceProvider.defaultVersion,circuitBreakerOptions);
    }


}
