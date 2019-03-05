package com.alisonyu.airforce.microservice.consumer;

import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.microservice.AirForceCircuitBreakerConfig;
import com.alisonyu.airforce.microservice.provider.ServiceProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vertx.core.Vertx;

import java.lang.reflect.Proxy;
import java.util.Objects;

public class ConsumerBuilder<T> {

    private Class<T> itf;
    private String version = ServiceProvider.defaultVersion;
    private String group = ServiceProvider.defaultGroup;
    private Vertx vertx;
    private CircuitBreakerConfig circuitBreakerConfig;
    private T fallbackInstance;

    public static <T> ConsumerBuilder<T> of(Vertx vertx,Class<T> itf){
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(itf);
        ConsumerBuilder<T> consumerBuilder = new ConsumerBuilder<>();
        consumerBuilder.vertx = vertx;
        consumerBuilder.itf = itf;
        return consumerBuilder;
    }

    public ConsumerBuilder<T> version(String version){
        this.version = version;
        return this;
    }

    public ConsumerBuilder<T> group(String group){
        this.group = group;
        return this;
    }

    public ConsumerBuilder<T> circuitBreakerConfig(CircuitBreakerConfig circuitBreakerConfig){
        this.circuitBreakerConfig = circuitBreakerConfig;
        return this;
    }

    public ConsumerBuilder<T> fallbackInstance(T fallbackInstance){
        if (!fallbackInstance.getClass().isAssignableFrom(itf)){
            throw new IllegalArgumentException();
        }
        this.fallbackInstance = fallbackInstance;
        return this;
    }

    public T create(){
        //use global circuitBreakerConfig if not specify
        if (circuitBreakerConfig == null){
            AirForceCircuitBreakerConfig config = AirForceEnv.getConfig(AirForceCircuitBreakerConfig.class);
            this.circuitBreakerConfig = config.getCircuitBreakerConfig();
        }
        ConsumeInvocationHandler invocationHandler = new ConsumeInvocationHandler(vertx,itf,group,version,circuitBreakerConfig,fallbackInstance);
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[]{itf},invocationHandler);
    }










}
