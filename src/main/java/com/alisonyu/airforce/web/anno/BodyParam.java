package com.alisonyu.airforce.web.anno;

import java.lang.annotation.*;

/**
 * @author yuzhiyi
 * @date 2018/9/15 9:19
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface BodyParam {
}
