package com.alisonyu.airforce.microservice.consumer;

import com.alisonyu.airforce.common.tool.async.MethodAsyncExecutor;
import com.alisonyu.airforce.core.config.SystemConfig;
import com.alisonyu.airforce.microservice.utils.MethodNameUtils;
import com.alisonyu.airforce.microservice.utils.ServiceMessageDeliveryOptions;
import com.alisonyu.airforce.ratelimiter.AirForceRateLimiterHelper;
import com.alisonyu.airforce.ratelimiter.AirforceRateLimiter;
import com.alisonyu.airforce.ratelimiter.RequestTooFastException;
import com.google.common.collect.Lists;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.utils.CircuitBreakerUtils;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.reactivex.RxHelper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, MethodAsyncExecutor> fallBackMethodMap = new HashMap<>();
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
        Class<?> returnType = method.getReturnType();
        Flowable<Object> flowable = Flowable.fromPublisher(publisher -> {
            //executor with circuitBreaker
            circuitBreaker.executeWithFallback(future -> {
                //remote call logic,the result will be represent as future
                eb.<Object>send(address,Lists.newArrayList(args), ServiceMessageDeliveryOptions.instance, as -> {
                    if (as.succeeded()){
                        Object result = as.result().body();
                        if (result instanceof Throwable){
                            future.fail((Throwable) result);
                        }
                        if (returnType == Void.TYPE){
                            future.complete(VOID);
                        }else{
                            future.complete(result);
                        }
                    }else{
                        future.fail(as.cause());
                    }
                });
            },v -> {
                //fallback logic
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
        else{
            throw new IllegalStateException("the service return type illegal!");
        }
    }



    public Object invoke2(Object proxy,Method method,Object[] args){
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = getCircuteBreaker2(method);
        AirforceRateLimiter rateLimiter = getRateLimiter(method);
        long startTime = System.nanoTime();
        Flowable<Object> remoteCallFlow = remoteCall(method,args)
                //check rate limit
                .doOnSubscribe(ignored -> {
                    if (rateLimiter != null){
                        if (!rateLimiter.acquirePermission()){
                            throw new RequestTooFastException();
                        }
                    }
                })
                //check circuteBreaker status
                .doOnSubscribe(ignored -> {
                    if (circuitBreaker != null){
                        CircuitBreakerUtils.isCallPermitted(circuitBreaker);
                    }
                })
                //the max waiting time of remote calling
                .timeout(3,TimeUnit.SECONDS)
                //mark error to circuitBreaker
                .doOnError(t ->{
                    if (circuitBreaker != null && !(t instanceof CircuitBreakerOpenException)){
                        long duration = System.nanoTime() - startTime;
                        circuitBreaker.onError(duration,t);
                    }
                })
                //mark success to circuteBreaker
                .doOnComplete(()-> {
                    if (circuitBreaker != null){
                        long duration = System.nanoTime() - startTime;
                        circuitBreaker.onSuccess(duration);
                    }
                })
                //if circuteBreaker open,trigger fallback method
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof CircuitBreakerOpenException){
                        Flowable<Object> fallbackFlow = executeFallbackMethod(method);
                        if (fallbackFlow == null){
                            return Flowable.error(throwable);
                        }else{
                            return fallbackFlow;
                        }
                    }else{
                        return Flowable.error(throwable);
                    }
                });

        return transfer(method,remoteCallFlow);

    }


    private Flowable<Object> remoteCall(Method method,Object[] args){
        EventBus eb = vertx.eventBus();
        String address = MethodNameUtils.getName(serviceClass,method,group,version);
        Class<?> returnType = method.getReturnType();
        return Flowable.fromPublisher(publisher -> {
            eb.<Object>send(address,Lists.newArrayList(args), ServiceMessageDeliveryOptions.instance, as -> {
                if (as.succeeded()){
                    Object result = as.result().body();
                    if (result instanceof Throwable){
                        publisher.onError((Throwable) result);
                    }
                    if (returnType == Void.TYPE){
                        publisher.onNext(VOID);
                        publisher.onComplete();
                    }else{
                        publisher.onNext(result);
                        publisher.onComplete();
                    }
                }else{
                    publisher.onError(as.cause());
                }
            });
        });
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


    private io.github.resilience4j.circuitbreaker.CircuitBreaker getCircuteBreaker2(Method method){
        //todo
        return null;
    }

    private AirforceRateLimiter getRateLimiter(Method method){
        //todo
        return null;
    }

    private Flowable<Object> executeFallbackMethod(Method method){
        //todo
        return null;
    }




}
