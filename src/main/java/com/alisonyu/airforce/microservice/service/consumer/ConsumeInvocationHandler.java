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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ConsumeInvocationHandler implements InvocationHandler {

    private Class serviceClass;
    private String group;
    private String version;
    private Vertx vertx;
    private DeliveryOptions options;

    public ConsumeInvocationHandler(Vertx vertx,Class<?> serviceClass,String group,String version){
        this.version = version;
        this.serviceClass = serviceClass;
        this.group = group;
        this.vertx = vertx;
        options = new DeliveryOptions();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Future<Object> future = Future.future();
        EventBus eb = vertx.eventBus();
        //构建参数
        JsonObject payload = new JsonObject().put("params", new JsonArray(Lists.newArrayList(args)));
        //获取服务调用地址
        String address = MethodNameUtils.getName(serviceClass,method,group,version);
        Flowable<Object> flowable = Flowable.fromPublisher(publisher->{
            eb.<JsonObject>send(address,payload.toString(),options,as -> {
                if (as.succeeded()){
                    JsonObject jsonResult = as.result().body();
                    ServiceResult serviceResult = jsonResult.mapTo(ServiceResult.class);
                    Class<?> clazz = Reflect.getClass(serviceResult.getClazz());
                    String json = serviceResult.getSerializeResult();
                    Object o = Json.decodeValue(json,clazz);
                    publisher.onNext(o);
                    publisher.onComplete();
                }else{
                    publisher.onError(as.cause());
                }
            });
        });

        //block result
        return flowable.blockingSingle();
    }


}
