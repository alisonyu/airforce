package com.alisonyu.airforce.web.executor.param;

import com.alisonyu.airforce.web.constant.ParamType;

import java.util.Objects;

/**
 * Rest接口方法参数元信息
 * @author yuzhiyi
 * @date 2018/9/12 11:10
 */
public class ParamMeta {
	/**
	 * 参数名
	 */
	private String name;
	/**
	 * 参数类型
	 */
	private Class<?> type;
	/**
	 * 参数被注解修饰的类型
	 */
	private ParamType paramType;

	/**
	 * 默认值
	 */
	private String defaultValue;


	public ParamMeta(String name,Class<?> type,String defaultValue){
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}


	public ParamType getParamType() {
		return paramType;
	}

	public void setParamType(ParamType paramType) {
		this.paramType = paramType;
	}

	@Override
	public String toString() {
		return "ParamMeta{" +
				"name='" + name + '\'' +
				", type=" + type +
				", paramType=" + paramType +
				", defaultValue=" + defaultValue +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParamMeta paramMeta = (ParamMeta) o;
		return Objects.equals(name, paramMeta.name) &&
				Objects.equals(type, paramMeta.type) &&
				paramType == paramMeta.paramType &&
				Objects.equals(defaultValue, paramMeta.defaultValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, paramType, defaultValue);
	}
}
