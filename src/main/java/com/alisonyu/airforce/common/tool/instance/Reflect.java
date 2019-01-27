package com.alisonyu.airforce.common.tool.instance;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author yuzhiyi
 * @date 2018/9/16 11:20
 */
public class Reflect {

	/**
	 * List<List<Person>>  => Person
	 */
	public static Type getBaseType(Type genericReturnType){
		if (genericReturnType instanceof ParameterizedType){
			Type[] actualTypeArguments = ((ParameterizedType)genericReturnType).getActualTypeArguments();
			Type type = actualTypeArguments[0];
			return getBaseType(type);
		}else{
			return genericReturnType;
		}
	}

	public static boolean isParameterizedType(Type type){
		return type instanceof ParameterizedType;
	}

	public static Type getRawType(Type type){
		Type out = type;
		if (isParameterizedType(type)){
			out = ((ParameterizedType)type).getRawType();
		}
		return out;
	}

	/**
	 *  List => Object
	 *  List<Person> => Person
	 */
	public static Type getListComponentType(Type type){
		Type out = Object.class;
		if (isParameterizedType(type)){
			Type[] actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments();
			if (actualTypeArguments.length>0){
				out = actualTypeArguments[0];
			}
		}
		return out;
	}

	public static Class<?> getClass(Type type){

		return getClass(type.getTypeName());
	}

	public static Class<?> getClass(String s){
		try {
			return Class.forName(s);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Object.class;
		}
	}


}
