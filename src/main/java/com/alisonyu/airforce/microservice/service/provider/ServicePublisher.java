package com.alisonyu.airforce.microservice.service.provider;

import com.alisonyu.airforce.microservice.service.utils.MethodNameUtils;
import com.alisonyu.airforce.tool.instance.Anno;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;

public class ServicePublisher {

    public void publish(Vertx vertx,Object instance){
        Class<?> clazz = instance.getClass();
        Class<?> itf = ServiceProvider.defaultClass;
        String group = ServiceProvider.defaultGroup;
        String version = ServiceProvider.defaultVersion;
        if (Anno.isMark(clazz,ServiceProvider.class)){
            itf = Anno.getAnnotationValue(clazz,"serviceClass",ServiceProvider.defaultClass,ServiceProvider.class);
            group = Anno.getAnnotationValue(clazz,"group",ServiceProvider.defaultGroup,ServiceProvider.class);
            version = Anno.getAnnotationValue(clazz,"version",ServiceProvider.defaultVersion,ServiceProvider.class);
        }
        /**
         * 如果没有标识该类的服务接口，取第一个interface
         */
        if (itf == ServiceProvider.defaultClass){
            itf = clazz.getInterfaces()[0];
        }
        final Class<?> finalItf = itf;
        final String finalGroup = group;
        final String finalVersion = version;
        Arrays.stream(itf.getDeclaredMethods())
                .forEach(method -> {
                    String name = MethodNameUtils.getName(finalItf,method,finalGroup,finalVersion);
                    EventBus eventBus = vertx.eventBus();
                    ServiceMethodProxy methodProxy = new ServiceMethodProxy(instance,method);
                    eventBus.<String>consumer(name, methodProxy::call);
                });
    }

}
