package com.alisonyu.airforce.ratelimiter;

import java.lang.reflect.Method;
import java.time.Duration;

public class AirForceRateLimiterHelper {

    public static AirforceRateLimitConfig getConfigFromMethod(Method method){
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit != null){
            int limit = rateLimit.limitForPeriod();
            long refreshPeriod = rateLimit.limitRefreshPeriod();
            long waitTime = rateLimit.timeoutDuration();
            AirforceRateLimitConfig config = new AirforceRateLimitConfig(Duration.ofMillis(waitTime),Duration.ofMillis(refreshPeriod),limit);
            return config;
        }else{
            return null;
        }
    }


}
