package com.alisonyu.airforce.common.tool.instance;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类扫描相关缓存池
 * @author yuzhiyi
 * @date 2018/9/14 10:42
 */
interface ClassPool {

	ConcurrentHashMap<Class<?>, Set<String>> fieldNamePool = new ConcurrentHashMap<>();

	ConcurrentHashMap<Class<?>,MethodAccess> methodAccessPool = new ConcurrentHashMap<>();

	ConcurrentHashMap<Class<?>,FieldAccess> fieldAccessPool = new ConcurrentHashMap<>();

}
