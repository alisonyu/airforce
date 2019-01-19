package com.alisonyu.airforce.microservice.service.provider;

import com.alisonyu.airforce.tool.AsyncHelper;
import com.alisonyu.airforce.tool.instance.Instance;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 服务方法代理类
 */
public class ServiceMethodProxy {


    private Object target;
    private Method proxyMethod;
    private DeliveryOptions deliveryOptions;

    public ServiceMethodProxy(Object instance, Method method){
        this.target = instance;
        this.proxyMethod = method;
        this.deliveryOptions = new DeliveryOptions();
    }


    public void call(Message<String> message){
        Object result = null;
        try{
            String json = message.body();
            JsonObject jsonObject = new JsonObject(json);
            JsonArray params = jsonObject.getJsonArray("params");
            Class<?>[] typeClasses = proxyMethod.getParameterTypes();
            Type[] types = proxyMethod.getGenericParameterTypes();
            if (params.size() != typeClasses.length){
                throw new IllegalArgumentException("参数数量不统一");
            }
            Object[] callParams = new Object[params.size()];
            for (int i=0;i<params.size();i++){
                Object param = Instance.cast(params.getValue(i),types[i],typeClasses[i]);
                callParams[i] = param;
            }
            result = proxyMethod.invoke(target,callParams);
        }catch (Throwable e){
            e.printStackTrace();
            result = e;
        }
        //处理Rxjava的异步返回
        if (result instanceof Publisher){
            Publisher<Object> observable = (Publisher<Object>)result;
            Flowable.fromPublisher(observable)
                    //在worker线程池执行逻辑
                    .subscribeOn(AsyncHelper.getBlockingScheduler())
                    .onErrorReturn(t -> t)
                    //.singleOrError()
                    .subscribe(o -> {
                        replyResult(o,message);
                    });
        }
        //处理Future的情况
        else if (result instanceof Future){
            Future<Object> future = (Future) result;
            future.setHandler(as -> {
                if (as.succeeded()){
                    replyResult(as.result(),message);
                }else{
                    replyResult(as.cause(),message);
                }
            });
        }
        //处理同步的情况
        else{
            replyResult(result,message);
        }


    }


    private void replyResult(Object result,Message message){
        ServiceResult serviceResult = new ServiceResult(result);
        JsonObject jsonResult  = JsonObject.mapFrom(serviceResult);
        //message.reply(jsonResult,deliveryOptions);
        message.reply(jsonResult.toString());
    }


}
