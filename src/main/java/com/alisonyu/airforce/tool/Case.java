package com.alisonyu.airforce.tool;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author yuzhiyi
 * @date 2018/9/12 11:36
 */
public class Case<T,R> {

	private T matcher;
	private Supplier<R> supplier;
	private Function<T,R> function;
	private static final Object WIDECARD = new Object();

	@SuppressWarnings("unchecked")
	public static <T,R>Case<T,R> of(T matcher,Supplier<R> supplier){
		Case c = new Case();
		c.setMatcher(matcher);
		c.setSupplier(supplier);
		return c;
	}

	@SuppressWarnings("unchecked")
	public static <T>Case<T,Void> of(T matcher,Runnable runnable){
		Case c = new Case();
		c.setMatcher(matcher);
		c.setSupplier(()->{ runnable.run(); return null; });
		return c;
	}

	@SuppressWarnings("unchecked")
	public static <T,R> Case<T,R> of(T matcher, Function<T,R> function){
		Case c = new Case();
		c.setMatcher(matcher);
		c.setFunction(function);
		return c;
	}

	@SuppressWarnings("unchecked")
	public static <T,R>Case<T,R> widecard(Supplier<R> supplier){
		Case c = new Case();
		c.setMatcher(WIDECARD);
		c.setSupplier(supplier);
		return c;
	}

	public boolean match(T in) {
		return matcher.equals(WIDECARD) || matcher.equals(in);
	}

	public T getMatcher() {
		return matcher;
	}

	void setMatcher(T matcher) {
		this.matcher = matcher;
	}

	public Supplier<R> getSupplier() {
		return supplier;
	}

	void setSupplier(Supplier<R> supplier) {
		this.supplier = supplier;
	}

	public Function<T, R> getFunction() {
		return function;
	}

	void setFunction(Function<T, R> function) {
		this.function = function;
	}
}
