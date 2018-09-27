package com.alisonyu.airforce.tool;
import org.junit.Test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yuzhiyi
 * @date 2018/9/18 15:01
 */
public class ReflectTest {

	public class Some{
		private String name;
		private Integer[] numbers;
		private List<String> list;
		private List<List<Double>> matrix;
		private Map<String,Class> map;
	}

	@Test
	public void queryNameType() throws NoSuchFieldException {
		Field field = Some.class.getDeclaredField("name");
		Class<?> type = field.getType();
		assertEquals(type,String.class);
	}

	@Test
	public void queryArrayType() throws NoSuchFieldException {
		Field field = Some.class.getDeclaredField("numbers");
		Class<?> type = field.getType();
		//一般来说，判断是否是某种类型是可以使用isAssignableFrom
		// 判断是否是数组类型比较特殊，要使用isArray()这个函数
		if (type.isArray()){
			//获得数组的类型，使用getComponentType()这个方法
			Class<?> componentType = type.getComponentType();
			assertEquals(componentType,Integer.class);
		}
		else{
			throw new IllegalStateException();
		}
	}

	@Test
	public void getListType() throws NoSuchFieldException {
		Field field = Some.class.getDeclaredField("list");
		//如果类似于List<String>这样的类型就是一种GenericType
		//注意这是一种Type类型
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType){
			//泛型参数类型
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type[] actualTypes = parameterizedType.getActualTypeArguments();
			//因为List<String>获得第一个泛型参数,因为只有一个，我们取第一个
			//如果我们有多个泛型参数，我们可以根据顺序取不同的泛型参数
			assertEquals(actualTypes[0],String.class);
			//如果获得List这个原始类型呢？
			assertEquals(parameterizedType.getRawType(),List.class);
		}else{
			throw new IllegalStateException();
		}
	}

	@Test
	public void getSubListType() throws NoSuchFieldException {
		//思考一下，如果我们有一个嵌套List，我们想拿到嵌套在最里面的类型，那么我们可以这么做呢？
		//其实我们可以使用递归的思想去获得最里面的类型
		Field field = Some.class.getDeclaredField("matrix");
		assertEquals(getBaseType(field.getGenericType()),Double.class);
	}

	public static Type getBaseType(Type genericReturnType){
		Objects.requireNonNull(genericReturnType);
		if (genericReturnType instanceof ParameterizedType &&
				List.class.isAssignableFrom((Class)(((ParameterizedType) genericReturnType).getRawType()))){
			Type[] actualTypeArguments = ((ParameterizedType)genericReturnType).getActualTypeArguments();
			Type type = actualTypeArguments[0];
			return getBaseType(type);
		}else{
			return genericReturnType;
		}
	}

	@Test
	public void getMapType() throws NoSuchFieldException {
		Field field = Some.class.getDeclaredField("map");
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type[] actualTypes = parameterizedType.getActualTypeArguments();
			assertEquals(actualTypes[0],String.class);
			assertEquals(actualTypes[1],Class.class);
		}else{
			throw new IllegalStateException();
		}
	}





}
