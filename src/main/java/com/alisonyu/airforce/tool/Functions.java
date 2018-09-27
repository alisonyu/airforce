package com.alisonyu.airforce.tool;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author yuzhiyi
 * @date 2018/9/12 11:24
 */
public class Functions {


	@SafeVarargs
	public static <T,R> R match(T o, Case<T,R> case1, Case<T,R>... cases){
		List<Case<T,R>> caseList = new LinkedList<>();
		caseList.add(case1);
		if (cases!= null){
			caseList.addAll(Arrays.asList(cases));
		}
		for (Case<T,R> c:caseList){
			if (c.match(o)){
				return c.getSupplier().get();
			}
		}
		return null;
	}

	@SafeVarargs
	public static <T,R> R matchAny(List<T> list,R defaultValue,Case<T,R> case1, Case<T,R>... cases){
		List<Case<T,R>> caseList = new LinkedList<>();
		caseList.add(case1);
		if (cases!= null){
			caseList.addAll(Arrays.asList(cases));
		}
		for (T o : list){
			for (Case<T,R> c : caseList){
				if (c.match(o)){
					if (c.getFunction() != null){
						return c.getFunction().apply(o);
					}else{
						return c.getSupplier().get();
					}
				}
			}
		}
		return defaultValue;
	}

	@SafeVarargs
	public static <T,R> R matchAny(List<T> list,Case<T,R> case1, Case<T,R>... cases){
		return matchAny(list,null ,case1, cases);
	}


	public static <K,V> V pool(final ConcurrentHashMap<K,V> pool,
							   final K key,
							   final Supplier<V>poolFn) {
		return pool.computeIfAbsent(key,(k)->poolFn.get());
	}









}
