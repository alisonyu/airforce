package com.alisonyu.airforce.microservice.core.exception;

import java.lang.annotation.*;

/**
 * 标识该方法用于处理异常信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandler {



}
