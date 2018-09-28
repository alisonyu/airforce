package com.alisonyu.airforce.tool.instance;

import com.alisonyu.airforce.tool.Case;
import com.alisonyu.airforce.tool.Matcher;
import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 反射类
 *
 * @author yuzhiyi
 * @date 2018/9/13 23:33
 */
public class Instance {

	private static Logger logger = LoggerFactory.getLogger(Instance.class);

	@SuppressWarnings("unchecked")
	static <T> T jvmInvoke(Object object, String methodName, Object... args) {
		Class clazz = object.getClass();
		try {
			return (T) clazz.getMethod(methodName).invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T enhanceInvoke(Object object, String methodName, Object... args) {
		MethodAccess methodAccess = getMethodAccess(object);
		try {
			return (T) methodAccess.invoke(object, methodName, args);
		} catch (Exception e) {
			throw e;
		}
	}

	private static boolean containField(Class<?> clazz, String fieldName) {
		Set<String> fieldSet = ClassPool.fieldNamePool.computeIfAbsent(clazz, (c) -> Arrays.stream(c.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet()));
		return fieldSet.contains(fieldName);
	}


	public static void enhanceSet(Object object, String fieldName, Object value) {
		if (!containField(object.getClass(), fieldName)) {
			return;
		}
		StringBuilder sb = new StringBuilder("set").append(fieldName);
		sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
		String setter = sb.toString();
		MethodAccess methodAccess = getMethodAccess(object);
		try {
			int idx = methodAccess.getIndex(setter);
			Class<?>[] types = methodAccess.getParameterTypes()[idx];
			if (types.length != 1) {
				throw new IllegalArgumentException("illegal setter method: " + setter);
			}
			Class<?> type = types[0];
			Object in = cast(value, type);
			methodAccess.invoke(object, setter, in);
		}
		//如果没有这个方法，那么降级为使用JVM设置
		catch (IllegalArgumentException e) {
			logger.warn(e.getMessage() + ",将降级使用JvmSet");
			jvmSet(object, fieldName, value);
		}

	}

	public static void jvmSet(Object object, String fieldName, Object value) {
		if (!containField(object.getClass(), fieldName)) {
			return;
		}
		try {
			Class<?> type = object.getClass();
			Object in = cast(value, type);
			Field field = type.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(object, in);
		} catch (NoSuchFieldException e) {
			//如果没有该field，直接忽略即可
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}


	private static Matcher<Class, Function<String, Object>> castMatcher = Matcher.of(Class.class,
			Case.of(String.class, () -> String::valueOf),
			Case.of(int.class, () -> Integer::valueOf),
			Case.of(Integer.class, () -> Integer::valueOf),
			Case.of(double.class, () -> Double::valueOf),
			Case.of(Double.class, () -> Double::valueOf),
			Case.of(float.class, () -> Float::valueOf),
			Case.of(Float.class, () -> Float::valueOf),
			Case.of(JsonObject.class, () -> JsonObject::new),
			Case.of(JsonArray.class, () -> JsonArray::new));

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object in, Class<T> type) {
		if (in == null) {
			return null;
		}
		Object out = null;
		if (in instanceof String) {
			Function<String, Object> fn = castMatcher.match(type);
			out = fn != null ? fn.apply((String) in) : in;
		} else if (type == String.class) {
			out = in.toString();
		} else if (in instanceof JsonArray) {
			if (type.isArray()) {
				//get the type of array
				Class<?> tClass = Reflect.getClass(type.getComponentType().getName());
				List list = ((JsonArray) in).getList();
				//instance array
				Object[] array = (Object[]) Array.newInstance(type.getComponentType(), list.size());
				for (int i = 0; i < array.length; i++) {
					Object o = list.get(i);
					if (o instanceof Map) {
						o = new JsonObject((Map) o);
					}
					array[i] = Instance.cast(o, tClass);
				}
				out = array;
			} else if (List.class.isAssignableFrom(type)) {
				//todo 因为List的类型会丢失的，因此要使用type来进行转化
				logger.warn("cast {} to list might occur some error! please use cast(Object in,Type type,Class<?> typeClazz)", in.getClass());
			}
		} else if (in instanceof JsonObject) {
			out = ((JsonObject) in).mapTo(type);
		} else {
			out = in;
		}
		return (T) out;
	}


	public static Object cast(Object in, Type type, Class<?> typeClazz) {
		Object out = null;
		//判断是否是List
		if (List.class.isAssignableFrom(typeClazz)) {
			if (in instanceof JsonArray) {
				out = castJsonArrayToList((JsonArray) in, type);
			}
		} else {
			out = cast(in, typeClazz);
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	private static List castJsonArrayToList(JsonArray jsonArray, Type listType) {
		if (!List.class.isAssignableFrom(Reflect.getClass(Reflect.getRawType(listType)))) {
			throw new IllegalArgumentException(listType.getTypeName() + " is not list");
		}
		Type componentType = Reflect.getListComponentType(listType);
		Class<?> componentClazz = Reflect.getClass(Reflect.getRawType(componentType));
		boolean componentIsList = List.class.isAssignableFrom(componentClazz);
		List list = new ArrayList(jsonArray.size());
		for (int i = 0; i < jsonArray.size(); i++) {
			if (componentIsList) {
				list.add(i, castJsonArrayToList(jsonArray.getJsonArray(i), componentType));
			} else {
				Object in = jsonArray.getValue(i);
				if (in instanceof Map) {
					in = new JsonObject((Map) in);
				}
				list.add(i, cast(in, componentClazz));
			}
		}
		return list;
	}


	public static <T> T instance(Class<T> clazz) {
		try {
			return clazz.newInstance();
			//todo 处理这些异常，并将异常报告给用户
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static MethodAccess getMethodAccess(Object object) {
		return ClassPool.methodAccessPool.computeIfAbsent(object.getClass(), MethodAccess::get);
	}

	private static FieldAccess getFieldAccess(Object o) {
		return ClassPool.fieldAccessPool.computeIfAbsent(o.getClass(), FieldAccess::get);
	}


}
