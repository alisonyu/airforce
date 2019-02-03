package com.alisonyu.airforce.ratelimiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

@Target({ElementType.TYPE ,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    long timeoutDuration() default 200;
    long limitRefreshPeriod() default 1000;
    int limitForPeriod() default  500;

}
