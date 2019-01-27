package com.alisonyu.airforce.microservice.consumer;

import com.alisonyu.airforce.microservice.provider.ServiceProvider;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class ConsumerProvider {

    public static <T> T getConsumer(Vertx vertx, Class<T> itf, String group, String version, CircuitBreakerOptions circuitBreakerOptions, Supplier<Object> fallbackFactory){
        Object fallbackInstance = fallbackFactory == null ? null : fallbackFactory.get();
        InvocationHandler invocationHandler = new ConsumeInvocationHandler(vertx,itf,group,version,circuitBreakerOptions,fallbackInstance);
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[]{itf},invocationHandler);
    }

    public static <T> T getConsumer(Vertx vertx,Class<T> itf){
        return getConsumer(vertx,itf, ServiceProvider.defaultGroup,ServiceProvider.defaultVersion,null,null);
    }

    public static <T> T getConsumer(Vertx vertx,Class<T> itf,CircuitBreakerOptions circuitBreakerOptions){
        return getConsumer(vertx,itf, ServiceProvider.defaultGroup,ServiceProvider.defaultVersion,circuitBreakerOptions,null);
    }

    public static <T> T getConsumer(Vertx vertx,Class<T> itf,CircuitBreakerOptions circuitBreakerOptions,Object fallbackInstance){
        return getConsumer(vertx,itf,
                ServiceProvider.defaultGroup,
                ServiceProvider.defaultVersion,
                circuitBreakerOptions,
                ()-> fallbackInstance);
    }

    public static <T> T getConsumerWithFallback(Vertx vertx,Class<T> itf,CircuitBreakerOptions circuitBreakerOptions,Supplier<Object> fallbackFactory){
        return getConsumer(vertx,itf,
                ServiceProvider.defaultGroup,
                ServiceProvider.defaultVersion,
                circuitBreakerOptions, fallbackFactory);
    }




}
