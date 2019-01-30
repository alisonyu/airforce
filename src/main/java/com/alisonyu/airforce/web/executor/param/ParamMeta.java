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
	/**
	 * 是否必传
	 */
	private boolean required = true;

	public ParamMeta(String name,Class<?> type,String defaultValue,boolean required){
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.required = required;
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

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public String toString() {
		return "ParamMeta{" +
				"name='" + name + '\'' +
				", type=" + type +
				", paramType=" + paramType +
				", defaultValue='" + defaultValue + '\'' +
				", required=" + required +
				'}';
	}
}
