package com.alisonyu.airforce.configuration.anno;

import java.lang.annotation.*;

/**
 * 用于配置扫描路径
 * @author yuzhiyi
 * @date 2018/9/15 9:07
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScanPackage {

	/**
	 * packageName
	 */
	String[] value();

}
