package com.alisonyu.airforce.ratelimiter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

public class AirforceRateLimiterImpl implements AirforceRateLimiter {

    private RateLimiter rateLimiter;
    private AirforceRateLimitConfig config;
    private final String name;
    private volatile boolean on = true;

    public AirforceRateLimiterImpl(String resourceName,AirforceRateLimitConfig config){
        this.name = resourceName;
        this.config = config;
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                                    .timeoutDuration(config.getTimeoutDuration())
                                    .limitForPeriod(config.getLimitForPeriod())
                                    .limitRefreshPeriod(config.getLimitRefreshPeriod())
                                    .build();

       rateLimiter = buildRateLimiterConfig(resourceName, config);
   }

   private RateLimiter buildRateLimiterConfig(String resourceName,AirforceRateLimitConfig config){
       RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
               .timeoutDuration(config.getTimeoutDuration())
               .limitForPeriod(config.getLimitForPeriod())
               .limitRefreshPeriod(config.getLimitRefreshPeriod())
               .build();

       return RateLimiter.of(resourceName,rateLimiterConfig);
   }


    @Override
    public boolean acquirePermission() {
        if (!on){
            return true;
        }
        return rateLimiter.getPermission(config.getTimeoutDuration());
    }

    /**
     *  dynamically change config
     */
    @Override
    public void changeConfig(AirforceRateLimitConfig config) {
        RateLimiter rateLimiter = buildRateLimiterConfig(this.name,config);
        synchronized (this){
            this.rateLimiter = rateLimiter;
            this.config = config;
        }
    }

    @Override
    public void turnOn() {
        synchronized (this){
            this.on = true;
        }
    }

    @Override
    public void turnOff() {
        synchronized (this){
            this.on = false;
        }
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    @Override
    public AirforceRateLimitConfig getConfig() {
        return config;
    }

    @Override
    public String getName() {
        return name;
    }
}
