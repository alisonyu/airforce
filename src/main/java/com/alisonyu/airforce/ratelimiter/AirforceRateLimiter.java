package com.alisonyu.airforce.ratelimiter;

import io.github.resilience4j.ratelimiter.operator.RateLimiterOperator;
import io.reactivex.FlowableOperator;

public interface AirforceRateLimiter {

    static AirforceRateLimiter of(String resourceName,AirforceRateLimitConfig config){
        AirforceRateLimiter limiter = new AirforceRateLimiterImpl(resourceName, config);
        RateLimiterManager.registerRateLimiter(limiter);
        return limiter;
    }

    static <T>FlowableOperator<T,T> rateLimitOperator(AirforceRateLimiter limiter){
        return RateLimiterOperator.of(((AirforceRateLimiterImpl)limiter).getRateLimiter());
    }


    boolean acquirePermission();

    default void changeConfig(AirforceRateLimitConfig config){
        throw new UnsupportedOperationException();
    }

    void turnOn();

    void turnOff();

    String getName();

    AirforceRateLimitConfig getConfig();

}
