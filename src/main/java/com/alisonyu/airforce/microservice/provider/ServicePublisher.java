package com.alisonyu.airforce.microservice.provider;

import com.alisonyu.airforce.microservice.utils.MethodNameUtils;
import com.alisonyu.airforce.common.tool.instance.Anno;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class ServicePublisher {

    private static Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    public static void publish(Vertx vertx,Object instance){
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

        /**
         * 获取限速相关的
         */


        Arrays.stream(itf.getDeclaredMethods())
                .forEach(method -> {
                    String name = MethodNameUtils.getName(finalItf,method,finalGroup,finalVersion);
                    EventBus eventBus = vertx.eventBus();
                    ServiceMethodProxy methodProxy = new ServiceMethodProxy(instance,method);
                    eventBus.<List<Object>>consumer(name, message -> {
                        //如果服务是由verticle来提供的，在其上下文执行
                        if (instance instanceof AbstractVerticle){
                            vertx.runOnContext(event -> {
                                methodProxy.call(message);
                            });
                        }
                        //如果服务不是在verticle提供的,在worker线程执行
                        else{
                            vertx.executeBlocking(event -> {
                                methodProxy.call(message);
                            },null);
                        }
                    });
                });

        logger.info("publish service:{} version:{} group:{} by instance {} successfully!",finalItf.getName(),finalVersion,finalGroup,instance.getClass().getName());
    }

}
