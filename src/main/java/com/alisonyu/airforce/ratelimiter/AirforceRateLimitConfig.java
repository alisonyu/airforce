package com.alisonyu.airforce.ratelimiter;

import java.time.Duration;
import java.util.Objects;

public class AirforceRateLimitConfig {

    private final Duration timeoutDuration;
    private final Duration limitRefreshPeriod;
    private final int limitForPeriod;

    public AirforceRateLimitConfig(Duration timeoutDuration, Duration limitRefreshPeriod, int limitForPeriod) {
        Objects.requireNonNull(timeoutDuration,"timeoutDuration can not be null");
        Objects.requireNonNull(limitRefreshPeriod,"limitRefreshPeriod can not be null");
        if (limitForPeriod <= 0){
            throw new IllegalArgumentException("limitForPeriod should be larger than zero");
        }
        this.timeoutDuration = timeoutDuration;
        this.limitRefreshPeriod = limitRefreshPeriod;
        this.limitForPeriod = limitForPeriod;
    }

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public Duration getLimitRefreshPeriod() {
        return limitRefreshPeriod;
    }

    public int getLimitForPeriod() {
        return limitForPeriod;
    }


}
