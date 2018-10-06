package com.alisonyu.airforce.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 容器接口
 * @author yuzhiyi
 * @date 2018/10/6 19:13
 */
public interface Container {


	Set<Class<?>> getClasses();

	Set<Class<?>> getClasses(Predicate<Class<?>> predicate);

	default Set<Class<?>> getClassWithAnnotation(Class<? extends Annotation> annotationClass){
		Objects.requireNonNull(annotationClass);
		return getClasses(c -> c.isAnnotationPresent(annotationClass));
	}

	/**
	 * 获取继承clazz的类（不包含clazz）
	 * @param clazz 被继承的类
	 * @param <T> 被继承的类型
	 * @return 继承clazz的类
	 */
	default <T> Set<Class<? extends T>> getClassesImpl(Class<T> clazz){
		Objects.requireNonNull(clazz);
		return (Set) getClasses(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && clazz.isAssignableFrom(c));
	}

}
