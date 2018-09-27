package com.alisonyu.airforce.tool.instance;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 管理已经扫描的类
 * @author yuzhiyi
 * @date 2018/9/25 10:26
 */
public class ScanedClass {

	private static volatile boolean scaned = false;

	public synchronized static void scan(){
		if (!scaned){
			ClassScaner
					.getClasses()
					.forEach(c -> ClassPool.classPool.put(c.getName(),c));
			scaned = true;
		}
	}

	public static Set<Class<?>> getClasses(Predicate<Class<?>> predicate){
		if (!scaned){
			scan();
		}
		Set<Class<?>> set = ClassPool.classPool.values().stream().filter(predicate).collect(Collectors.toSet());
		return Collections.unmodifiableSet(set);
	}

}
