package com.alisonyu.airforce.tool;

import com.alisonyu.airforce.tool.instance.Instance;
import com.alisonyu.airforce.tool.instance.Reflect;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InstanceTest {

	@Test
	public void set() {
		Person person = Instance.instance(Person.class);
		HashMap<String,String> params = new HashMap<>();
		params.put("name","alisonyu");
		params.put("age","20");
		params.forEach((k,v)-> Instance.enhanceSet(person,k,v));
		System.out.println(person);
	}


	@Test
	public void setterTest(){
		String fieldName = "name";
		StringBuilder sb = new StringBuilder("set").append(fieldName);
		sb.setCharAt(3,Character.toUpperCase(sb.charAt(3)));
		String setter = sb.toString();
		System.out.println(setter);
	}

	@Test
	public void test() throws NoSuchFieldException {
		Field field = some.class.getDeclaredField("lists");
		Type type = field.getGenericType();
		System.out.println(type);
		System.out.println(Reflect.getRawType(type));
		System.out.println(Reflect.getListComponentType(type));
	}


	public static class some{
		List<List<Person>>  lists = new ArrayList<>();
	}


}