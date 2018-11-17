package com.alisonyu.airforce.common;

import com.alisonyu.airforce.tool.instance.ClassScaner;
import com.alisonyu.airforce.tool.instance.Reflect;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Spring容器
 * @author yuzhiyi
 * @date 2018/10/7 20:23
 */
public class SpringAirForceContainer implements Container {

	private final Vertx vertx;

	private final SpringConfiguration springConfiguration;

	private ConfigurableApplicationContext applicationContext;

	private AtomicBoolean inited = new AtomicBoolean(false);

	private ConcurrentHashSet<Class<?>> clazzPool;


	public SpringAirForceContainer(Vertx vertx,SpringConfiguration springConfiguration){
		this.vertx = vertx;
		this.springConfiguration = springConfiguration;
	}

	private synchronized void init(){
		if (!hasInit()){
			Set<Class<?>> classes = ClassScaner.getClasses();
			clazzPool = new ConcurrentHashSet<>();
			clazzPool.addAll(classes);
			Class<?> configuration = Reflect.getClass(springConfiguration.getConfigurationClass());
			startSpringBoot(configuration,getGenericApplicationContext());
			this.inited.set(true);
		}

	}

	private GenericApplicationContext getGenericApplicationContext(){
		//1、初始化ApplicationContext
		GenericApplicationContext context = new GenericApplicationContext();
		context.setClassLoader(vertx.getClass().getClassLoader());
		//2、将Vertx加入容器中
		context.getBeanFactory().registerSingleton(Vertx.class.getCanonicalName(),vertx);
		context.refresh();
		return context;
	}

	private void startSpringBoot(Class<?> configurationClass,GenericApplicationContext parent){
		SpringApplicationBuilder builder = new SpringApplicationBuilder(configurationClass);
		builder.parent(parent);
		builder.web(WebApplicationType.NONE);
		this.applicationContext = builder.run();
	}


	@Override
	public Object injectObject(Object o) {
		Objects.requireNonNull(o);
		ensureInit();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(o);
		applicationContext.getAutowireCapableBeanFactory().initializeBean(o,"asdf");
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
