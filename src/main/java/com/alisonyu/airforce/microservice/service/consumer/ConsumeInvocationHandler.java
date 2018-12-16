package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.airforce.microservice.service.provider.ServiceResult;
import com.alisonyu.airforce.microservice.service.utils.MethodNameUtils;
import com.alisonyu.airforce.tool.instance.Instance;
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
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ConsumeInvocationHandler implements InvocationHandler {

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
        JsonArray arr = args == null ? new JsonArray() : new JsonArray(Lists.newArrayList(args));
        //构建参数
        JsonObject payload = new JsonObject().put("params", arr);
        //获取服务调用地址
        String address = MethodNameUtils.getName(serviceClass,method,group,version);
        Class<? > returnType = method.getReturnType();
        Flowable<Object> flowable = Flowable.fromPublisher(publisher->{
            eb.<JsonObject>send(address,payload.toString(),options,as -> {
                if (as.succeeded()){
                    if (returnType == Void.TYPE){
                        publisher.onNext(VOID);
                        publisher.onComplete();
                    }else{
                        JsonObject jsonResult = as.result().body();
                        ServiceResult serviceResult = jsonResult.mapTo(ServiceResult.class);
                        Class<?> clazz = Reflect.getClass(serviceResult.getClazz());
                        String json = serviceResult.getSerializeResult();
                        Object o = Json.decodeValue(json,clazz);
                        publisher.onNext(o);
                        publisher.onComplete();
                    }
                }else{
                    publisher.onError(as.cause());
                }
            });
        });
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
            Object res = flowable.blockingSingle();
            return res == VOID ? null : res;
        }
    }




}
