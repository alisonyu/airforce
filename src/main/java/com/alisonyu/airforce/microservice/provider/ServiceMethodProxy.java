package com.alisonyu.airforce.microservice.provider;

import com.alisonyu.airforce.common.tool.async.MethodAsyncExecutor;
import com.alisonyu.airforce.microservice.utils.ServiceMessageDeliveryOptions;
import com.alisonyu.airforce.ratelimiter.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * 服务方法代理类
 */
public class ServiceMethodProxy {

    private Logger logger = LoggerFactory.getLogger(ServiceMethodProxy.class);

    private final Object target;
    private final Method proxyMethod;
    private AirforceRateLimiter rateLimiter;
    private MethodAsyncExecutor methodAsyncExecutor;

    public ServiceMethodProxy(Object instance, Method method){
        this.target = instance;
        this.proxyMethod = method;
        initMethodAsyncExecutor();
        initRateLimiter();
    }


    /**
     * if service instanceOf abstractVerticle,the proxy method will be executed on context thread,else it will be executed on worker threads
     */
    private void initMethodAsyncExecutor(){
        Context context = null;
        if (this.target instanceof AbstractVerticle){
            AbstractVerticle v = (AbstractVerticle) target;
            context = v.getVertx().getOrCreateContext();
        }
        this.methodAsyncExecutor = new MethodAsyncExecutor(this.target,this.proxyMethod,context);
    }

    private void initRateLimiter(){
        Method method = this.proxyMethod;
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit != null){
            String resourceName = method.getName() + "#" + "service";
            AirforceRateLimitConfig config = AirForceRateLimiterHelper.getConfigFromMethod(method);
            this.rateLimiter = AirforceRateLimiter.of(resourceName,config);
        }
    }


    public void call(Message<List<Object>> message){
        List<Object> callParams = message.body();
        Object[] params = Optional.ofNullable(message.body())
                .map(List::toArray)
                .orElse(new Object[0]);
        methodAsyncExecutor.invoke(params)
                .doOnSubscribe(ignored -> rateLimitCheck())
                .doOnError(e -> logger.error(e.getMessage(),e))
                .onErrorReturn(e -> e)
                .subscribe(r -> replyResult(r,message));

    }


    private void replyResult(Object result,Message message){
        message.reply(result, ServiceMessageDeliveryOptions.instance);
    }

    private void rateLimitCheck(){
        if (this.rateLimiter != null){
            boolean permission = this.rateLimiter.acquirePermission();
            if (!permission){
                throw new RequestTooFastException();
            }
        }
    }

}
