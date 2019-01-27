package com.alisonyu.airforce.common.tool.functional;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 提供一个可复用的匹配函数
 * @author yuzhiyi
 * @date 2018/9/13 18:25
 */
public class Matcher<T,R> {

	private Class<T> type;
	private List<Case<T,R>> cases;


	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T,R>Matcher<T,R> of(Class<T> type,Case<T,R>case1 ,Case<T,R>... cases){
		Matcher matcher = new Matcher();
		matcher.type = type;
		matcher.cases = new LinkedList();
		matcher.cases.add(case1);
		matcher.cases.addAll(Arrays.asList(cases));
		return matcher;
	}

	public R match(T o){
		for (Case<T,R> c: cases){
			if (c.match(o)){
				return c.getSupplier().get();
			}
		}
		return null;
	}

}
