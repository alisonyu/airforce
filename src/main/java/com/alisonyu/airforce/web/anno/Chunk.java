package com.alisonyu.airforce.web.anno;

import java.lang.annotation.*;

/**
 * 表示Rest Method的返回值是流式的
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Chunk {

}
