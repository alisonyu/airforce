package com.alisonyu.airforce.tool;

import io.vertx.core.Future;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author yuzhiyi
 * @date 2018/9/16 10:56
 */
public class GenericParameterizedTypeDemo {

	public static void main(String[] args) throws Exception {
		//通过反射获取到方法
		Method declaredMethod = GenericParameterizedTypeDemo.class.getDeclaredMethod("findStr", int.class,Map.class);
		//获取返回值的类型，此处不是数组，请注意智商，返回值只能是一个
		Type genericReturnType = declaredMethod.getGenericReturnType();
		System.out.println(genericReturnType);
		System.out.println(getLastType(genericReturnType));
		//获取返回值的泛型参数
//		if(genericReturnType instanceof ParameterizedType){
//			Type[] actualTypeArguments = ((ParameterizedType)genericReturnType).getActualTypeArguments();
//			for (Type type : actualTypeArguments) {
//				System.out.println(type);
//			}
//		}
	}


	private static Type getLastType(Type genericReturnType){
		if (genericReturnType instanceof ParameterizedType){
			Type[] actualTypeArguments = ((ParameterizedType)genericReturnType).getActualTypeArguments();
			Type type = actualTypeArguments[0];
			return getLastType(type);
		}else{
			return genericReturnType;
		}
	}


	public static Future<List<Person>> findStr(int id, Map<Integer, String> map){
		return null;
	}



}
