package com.alisonyu.airforce.cluster.config.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示启用Cloud相关功能
 * @author yuzhiyi
 * @date 2018/9/22 15:22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAirforceCloud {
}
