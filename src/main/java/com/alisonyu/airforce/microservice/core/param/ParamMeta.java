package com.alisonyu.airforce.microservice.core.param;

import com.alisonyu.airforce.constant.ParamType;
import com.alisonyu.airforce.tool.Case;
import com.alisonyu.airforce.tool.Functions;

import java.util.function.Function;

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
}
