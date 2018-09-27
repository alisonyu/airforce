package com.alisonyu.airforce.microservice.anno;

import java.lang.annotation.*;

/**
 * @author yuzhiyi
 * @date 2018/9/15 9:24
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SessionParam {

	String value();

}
