package com.alisonyu.airforce.common;

import com.alisonyu.airforce.tool.instance.ClassScaner;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * AirForce默认容器
 * @author yuzhiyi
 * @date 2018/10/6 19:19
 */
public class AirForceContainer implements Container{


	private AtomicBoolean inited = new AtomicBoolean(false);

	private ConcurrentHashSet<Class<?>> clazzPool;

	private synchronized void init(){
		if (!hasInit()){
			Set<Class<?>> classes = ClassScaner.getClasses();
			clazzPool = new ConcurrentHashSet<>();
			clazzPool.addAll(classes);
			this.inited.set(true);
		}
	}

	/**
	 * 默认的容器没有依赖注入
	 * @param o
	 * @return
	 */
	@Override
	public Object injectObject(Object o) {
		return o;
	}

	@Override
	public Set<Class<?>> getClasses() {
		ensureInit();
		return Collections.unmodifiableSet(clazzPool);
	}

	@Override
	public Set<Class<?>> getClasses(Predicate<Class<?>> predicate) {
		Objects.requireNonNull(predicate);
		ensureInit();
		Set<Class<?>> set = clazzPool.stream().filter(predicate).collect(Collectors.toSet());
		return Collections.unmodifiableSet(set);
	}

	private boolean hasInit(){
		return inited.get();
	}

	private void ensureInit(){
		if (!hasInit()){
			init();
		}
	}


}
