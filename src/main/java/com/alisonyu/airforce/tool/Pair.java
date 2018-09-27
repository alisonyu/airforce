package com.alisonyu.airforce.tool;

/**
 * @author yuzhiyi
 * @date 2018/9/17 16:13
 */
public class Pair<K,V> {

	private K key;
	private V value;

	public static <K,V>Pair<K,V> of(K key,V val){
		Pair<K,V> pair = new Pair<K,V>();
		pair.key = key;
		pair.value = val;
		return pair;
	}

	public K getKey() {
		return key;
	}


	public V getValue() {
		return value;
	}


}
