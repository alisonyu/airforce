package com.alisonyu.airforce.ratelimiter;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE ,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 等待的最大时间，单位ms
     */
    long timeoutDuration() default 200;

    /**
     * 刷新的时间
     * @return
     */
    long limitRefreshPeriod() default 1000;

    /**
     * 限速的次数
     * @return
     */
    int limitForPeriod() default  500;

}
