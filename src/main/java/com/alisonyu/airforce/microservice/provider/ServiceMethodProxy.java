package com.alisonyu.airforce.microservice.provider;

import com.alisonyu.airforce.microservice.utils.ServiceMessageDeliveryOptions;
import com.alisonyu.airforce.common.tool.AsyncHelper;
import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.reactivestreams.Publisher;

import java.lang.reflect.Method;
import java.util.List;

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


    public void call(Message<List<Object>> message){
        Object result = null;
        try{
            List<Object> callParams = message.body();
            result = proxyMethod.invoke(target,callParams.toArray());
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
        message.reply(result, ServiceMessageDeliveryOptions.instance);
    }

}
