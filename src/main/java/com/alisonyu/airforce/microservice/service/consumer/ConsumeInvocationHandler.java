package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.airforce.microservice.service.provider.ServiceResult;
import com.alisonyu.airforce.microservice.service.utils.MethodNameUtils;
import com.alisonyu.airforce.microservice.service.utils.ServiceMessageDeliveryOptions;
import com.alisonyu.airforce.tool.AsyncHelper;
import com.alisonyu.airforce.tool.instance.Reflect;
import com.google.common.collect.Lists;
import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class ConsumeInvocationHandler implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(ConsumeInvocationHandler.class);
    private Class serviceClass;
    private String group;
    private String version;
    private Vertx vertx;
    private DeliveryOptions options;
    private static Object VOID = new Object();

    public ConsumeInvocationHandler(Vertx vertx,Class<?> serviceClass,String group,String version){
        this.version = version;
        this.serviceClass = serviceClass;
        this.group = group;
        this.vertx = vertx;
        options = new DeliveryOptions();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        EventBus eb = vertx.eventBus();
        //获取服务调用地址
        String address = MethodNameUtils.getName(serviceClass,method,group,version);
        Class<? > returnType = method.getReturnType();
        Flowable<Object> flowable = Flowable.fromPublisher(publisher->{
            eb.<Object>send(address,Lists.newArrayList(args), ServiceMessageDeliveryOptions.instance, as -> {
                if (as.succeeded()){
                    if (returnType == Void.TYPE){
                        publisher.onNext(VOID);
                        publisher.onComplete();
                    }else{
                        Object result = as.result().body();
                        publisher.onNext(result);
                        publisher.onComplete();
                    }
                }else{
                    publisher.onError(as.cause());
                }
            });
        })
        //3秒超时
        .timeout(3, TimeUnit.SECONDS);

        //暂时只允许消费Flowable
        if (Flowable.class.isAssignableFrom(returnType)){
            return flowable;
        }
        //如果是Future
        else if (Future.class.isAssignableFrom(returnType)){
            Future<Object> future = Future.future();
            flowable
                    .doOnError(future::fail)
                    .subscribe(future::complete);
            return future;
        }
        //否则同步获取结果
        else {
            //block result
            AtomicReference<Object> res = new AtomicReference<>();
            flowable
                    .observeOn(AsyncHelper.getBlockingScheduler())
                    .doOnError(e -> {
                        if (e instanceof TimeoutException){
                            logger.error("call {} timeout 3000",method.toGenericString());
                        }
                        throw new RuntimeException(e);
                    })
                    .blockingSubscribe(o -> {
                        res.set(o);
                    } );
            Object realResult = res.get();
            //Object realResult = flowable.blockingFirst();
            return realResult == VOID ? null : realResult;
        }
    }




}
