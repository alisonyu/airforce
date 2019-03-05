package com.alisonyu.airforce.microservice.consumer;

import com.alisonyu.airforce.common.tool.async.MethodAsyncExecutor;
import com.alisonyu.airforce.microservice.utils.MethodNameUtils;
import com.alisonyu.airforce.microservice.utils.ServiceMessageDeliveryOptions;
import com.alisonyu.airforce.ratelimiter.AirforceRateLimiter;
import com.google.common.collect.Lists;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.utils.CircuitBreakerUtils;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.reactivex.RxHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConsumeInvocationHandler implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(ConsumeInvocationHandler.class);
    private Class serviceClass;
    private String group;
    private String version;
    private Vertx vertx;
    private CircuitBreakerOptions circuitBreakerOptions;
    private CircuitBreakerConfig circuitBreakerConfig;
    private final Object fallBackInstance;
    private Map<Method, MethodAsyncExecutor> fallBackMethodMap = new ConcurrentHashMap<>();
    private Map<Method, io.github.resilience4j.circuitbreaker.CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();
    private static Object VOID = new Object();


    public ConsumeInvocationHandler(Vertx vertx,Class<?> serviceClass,String group,String version,CircuitBreakerConfig circuitBreakerConfig,Object fallbackInstance){
        this.version = version;
        this.serviceClass = serviceClass;
        this.group = group;
        this.vertx = vertx;
        this.fallBackInstance = fallbackInstance;
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    public void setCircuitBreakerConfig(CircuitBreakerConfig circuitBreakerConfig){
        this.circuitBreakerConfig = circuitBreakerConfig;
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
        else if (Void.class.isAssignableFrom(returnType)){
            return null;
        }
        else{
            throw new IllegalStateException("the service return type illegal!");
        }
    }



    public Object invoke2(Object proxy,Method method,Object[] args){
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = getCircuitBreaker2(method);
        long startTime = System.nanoTime();
        Flowable<Object> remoteCallFlow = remoteCall(method,args)
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
                        Flowable<Object> fallbackFlow = executeFallbackMethod(method,args);
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
        return invoke2(proxy,method,args);
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


    private io.github.resilience4j.circuitbreaker.CircuitBreaker getCircuitBreaker2(Method method){
        return circuitBreakerMap.computeIfAbsent(method,ignored -> {
            if (circuitBreakerConfig == null){
                return null;
            }else{
                String resourceName = method.getName() + "#" + "circuitBreaker";
                return io.github.resilience4j.circuitbreaker.CircuitBreaker.of(resourceName,circuitBreakerConfig);
            }
        });
    }

    private Flowable<Object> executeFallbackMethod(Method method,Object[] args){
        if (fallBackInstance == null){
            return null;
        }else{
            MethodAsyncExecutor executor = fallBackMethodMap.computeIfAbsent(method,key-> {
                try {
                     Method fallbackMethod = fallBackInstance.getClass().getMethod(method.getName(),method.getParameterTypes());
                     Context context = null;
                     if (fallBackInstance instanceof AbstractVerticle){
                         context = ((AbstractVerticle) fallBackInstance).getVertx().getOrCreateContext();
                     }
                     MethodAsyncExecutor asyncExecutor = new MethodAsyncExecutor(fallBackInstance,method,context);
                     return asyncExecutor;

                } catch (NoSuchMethodException e) {
                    logger.warn("cannot find fallback method {} from {}",method.getName(),fallBackInstance.getClass());
                    return null;
                }
            });
            if (executor != null){
                return executor.invoke(args);
            }else{
                return null;
            }
        }
    }




}
