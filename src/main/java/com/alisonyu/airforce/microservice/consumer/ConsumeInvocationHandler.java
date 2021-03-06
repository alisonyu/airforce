package com.alisonyu.airforce.microservice.consumer;

import com.alisonyu.airforce.microservice.utils.MethodNameUtils;
import com.alisonyu.airforce.microservice.utils.ServiceMessageDeliveryOptions;
import com.alisonyu.airforce.common.tool.async.AsyncHelper;
import com.google.common.collect.Lists;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.reactivex.RxHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class ConsumeInvocationHandler implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(ConsumeInvocationHandler.class);
    private Class serviceClass;
    private String group;
    private String version;
    private Vertx vertx;
    private CircuitBreakerOptions circuitBreakerOptions;
    private Scheduler blockingScheduler;
    private final Object fallBackInstance;
    private static Object VOID = new Object();


    public ConsumeInvocationHandler(Vertx vertx,Class<?> serviceClass,String group,String version,CircuitBreakerOptions circuitBreakerOptions,Object fallbackInstance){
        this.version = version;
        this.serviceClass = serviceClass;
        this.group = group;
        this.vertx = vertx;
        this.blockingScheduler = RxHelper.blockingScheduler(vertx,false);
        this.fallBackInstance = fallbackInstance;
        this.circuitBreakerOptions = circuitBreakerOptions;
    }

    public Object invoke1(Object proxy, Method method, Object[] args) throws Throwable {
        EventBus eb = vertx.eventBus();
        CircuitBreaker circuitBreaker = getCircuitBreaker(method);
        String address = MethodNameUtils.getName(serviceClass,method,group,version);
        Class<? > returnType = method.getReturnType();
        //todo 将方法调用变为AsyncMethodExecutor
        //todo 将这段方法抽出来，以实现泛化调用
        Flowable<Object> flowable = Flowable.fromPublisher(publisher -> {
            //executor with circuitBreaker
            circuitBreaker.executeWithFallback(future -> {
                eb.<Object>send(address,Lists.newArrayList(args), ServiceMessageDeliveryOptions.instance, as -> {
                    if (as.succeeded()){
                        if (returnType == Void.TYPE){
                            future.complete(VOID);
                        }else{
                            Object result = as.result().body();
                            future.complete(result);
                        }
                    }else{
                        future.fail(as.cause());
                    }
                });
            },v -> {
                //todo do log
                logger.error("triger fallback");
                if (this.fallBackInstance == null){
                    throw new RuntimeException(v);
                }
                Flowable<Object> fallbackFlowable;
                try {
                    Object o = method.invoke(fallBackInstance,args);
                    if (o instanceof Flowable){
                        fallbackFlowable = (Flowable) o;
                    }else if (o instanceof Future){
                        Future<Object> future = (Future) o;
                        fallbackFlowable = Flowable.fromPublisher(fallbackPublisher -> {
                            ((Future<Object>) o).setHandler(as -> {
                                  if (as.succeeded()){
                                      fallbackPublisher.onNext(as.result());
                                      fallbackPublisher.onComplete();
                                  }else{
                                      fallbackPublisher.onError(as.cause());
                                  }
                            });
                        });
                    }else{
                        fallbackFlowable = Flowable.just(o);
                    }
                    return transfer(method,fallbackFlowable);
                } catch (Exception e) {
                    logger.error("run fallback method error",e);
                    fallbackFlowable = Flowable.fromPublisher(errorPublisher -> errorPublisher.onError(e));
                }
                return transfer(method,fallbackFlowable);
            }).setHandler(ar -> {
                if (ar.succeeded()){
                    Object result = ar.result();
                    //if fallback method invoke,this block will be executed
                    if (result instanceof Flowable){
                        ((Flowable) result).subscribe(o-> {
                            publisher.onNext(o);
                            publisher.onComplete();
                        });
                    }else{
                        publisher.onNext(result);
                        publisher.onComplete();
                    }
                }else{
                    publisher.onError(ar.cause());
                }
            });
        });
        return transfer(method,flowable);
    }



    public Object invoke0(Object proxy, Method method, Object[] args) throws Throwable {
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
        return transfer(method,flowable);
    }

    private Object transfer(Method method,Flowable<Object> flowable){
        Class<? > returnType = method.getReturnType();
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
        //todo 移除同步该分支
        //否则同步获取结果
        else {
            //block result
            AtomicReference<Object> res = new AtomicReference<>();
            flowable
                    .observeOn(this.blockingScheduler)
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
            return realResult == VOID ? null : realResult;
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoke1(proxy,method,args);
    }


    private ConcurrentHashMap<String,CircuitBreaker> circuitBreakerInstancePool = new ConcurrentHashMap<>();
    private CircuitBreaker getCircuitBreaker(Method method){
        String key = method.toGenericString();
        return circuitBreakerInstancePool.computeIfAbsent(key, name -> {
            CircuitBreakerOptions options =  this.circuitBreakerOptions == null ? new CircuitBreakerOptions() : this.circuitBreakerOptions;
            CircuitBreaker circuitBreaker = CircuitBreaker.create(name,vertx,options);
            return circuitBreaker;
        });
    }




}
