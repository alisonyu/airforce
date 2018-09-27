package com.alisonyu.airforce.microservice.anno;

import java.lang.annotation.*;

/**
 * 用于标识接口是一个同步方法，
 * 被该注解修饰的方法将会在Worker线程中执行
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sync {
}
