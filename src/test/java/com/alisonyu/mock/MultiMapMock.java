package com.alisonyu.mock;

import io.vertx.core.MultiMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于mock MultiMap
 * @author yuzhiyi
 * @date 2018/9/27 23:00
 */
public class MultiMapMock implements MultiMap {

	private Map<String,String> map = new HashMap<>();


	@Override
	public String get(CharSequence name) {
		return map.get(name.toString());
	}

	@Override
	public String get(String name) {
		return map.get(name);
	}

	@Override
	public List<String> getAll(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getAll(CharSequence name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Map.Entry<String, String>> entries() {
		return new ArrayList<>(map.entrySet());
	}

	@Override
	public boolean contains(String name) {
		return map.containsKey(name);
	}

	@Override
	public boolean contains(CharSequence name) {
		return map.containsKey(name.toString());
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<String> names() {
		return map.keySet();
	}

	@Override
	public MultiMap add(String name, String value) {
		map.put(name,value);
		return this;
	}

	@Override
	public MultiMap add(CharSequence name, CharSequence value) {
		map.put(name.toString(),value.toString());
		return this;
	}

	@Override
	public MultiMap add(String name, Iterable<String> values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MultiMap add(CharSequence name, Iterable<CharSequence> values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MultiMap addAll(MultiMap map) {
		map.entries().forEach(entry -> this.map.put(entry.getKey(),entry.getValue()));
		return this;
	}

	@Override
	public MultiMap addAll(Map<String, String> headers) {
		this.map.putAll(headers);
		return this;
	}

	@Override
	public MultiMap set(String name, String value) {
		this.map.put(name,value);
		return this;
	}

	@Override
	public MultiMap set(CharSequence name, CharSequence value) {
		return this.set(name.toString(),value.toString());
	}

	@Override
	public MultiMap set(String name, Iterable<String> values) {
		throw new UnsupportedOperationException();

	}

	@Override
	public MultiMap set(CharSequence name, Iterable<CharSequence> values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MultiMap setAll(MultiMap map) {
		map.entries().forEach(entry -> this.map.put(entry.getKey(),entry.getValue()));
		return this;
	}

	@Override
	public MultiMap setAll(Map<String, String> headers) {
		map.putAll(headers);
		return this;
	}

	@Override
	public MultiMap remove(String name) {
		map.remove(name);
		return this;
	}

	@Override
	public MultiMap remove(CharSequence name) {
		return this.remove(name.toString());
	}

	@Override
	public MultiMap clear() {
		map.clear();
		return this;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Iterator<Map.Entry<String, String>> iterator() {
		return map.entrySet().iterator();
	}
}
