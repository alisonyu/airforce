package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.airforce.microservice.service.provider.ServiceProvider;
import io.vertx.core.Vertx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ConsumerProvider {

    public static <T> T getConsumer(Vertx vertx, Class<T> itf, String group, String version){
        InvocationHandler invocationHandler = new ConsumeInvocationHandler(vertx,itf,group,version);
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[]{itf},invocationHandler);
    }

    public static <T> T getConsumer(Vertx vertx,Class<T> itf){
        return getConsumer(vertx,itf, ServiceProvider.defaultGroup,ServiceProvider.defaultVersion);
    }


}
