package com.alisonyu.airforce.microservice.core.param;
import com.alisonyu.airforce.constant.ParamType;
import com.alisonyu.example.test.Person;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class ParamAcceptorTest {

	@Test
	public void getStringValue() {
		ParamMeta paramMeta = new ParamMeta("hello",String.class,"");
		//测试正常情况和空情况
		Object[] ins = {"hello",null,123456};
		Object[] expects = {"hello","","123456"};
		for (int idx = 0; idx < ins.length ;idx++){
			Object out = ParamAcceptor.getValue(paramMeta,ins[idx]);
			assertEquals(expects[idx],out);
		}

	}

	@Test
	public void getNumberValue(){
		ParamMeta paramMeta = new ParamMeta("account",Integer.class,null);
		//正常的输入
		Object[] ins = {123456,"123456"};
		Object[] expects = {123456,123456};
		for (int idx = 0;idx < ins.length; idx++){
			Object out = ParamAcceptor.getValue(paramMeta,ins[idx]);
			assertEquals(expects[idx],out);
		}
	}

	@Test(expected = NumberFormatException.class)
	public void testToBigNumber(){
		ParamMeta paramMeta = new ParamMeta("account",Integer.class,null);
		Object in = "10000000000000000000000000000000000";
		ParamAcceptor.getValue(paramMeta,in);
	}

	@Test(expected = NumberFormatException.class)
	public void testToBigNumber2(){
		ParamMeta paramMeta = new ParamMeta("account",Integer.class,null);
		Object in = 1111111111111111L;
		ParamAcceptor.getValue(paramMeta,in);
	}

	@Test(expected = NumberFormatException.class)
	public void testInvalidInput(){
		ParamMeta paramMeta = new ParamMeta("account",Integer.class,null);
		ArrayList list = new ArrayList();
		ParamAcceptor.getValue(paramMeta,list);
	}

	@Test(expected = NumberFormatException.class)
	public void testInvalidInput2(){
		ParamMeta paramMeta = new ParamMeta("account",Integer.class,null);
		String input = "asdfghj";
		ParamAcceptor.getValue(paramMeta,input);
	}

	@Test
	public void testJsonObject(){
		ParamMeta paramMeta = new ParamMeta("jsonObject", JsonObject.class,null);
		paramMeta.setParamType(ParamType.BODY_PARAM);
		MultiMap map = MultiMap.caseInsensitiveMultiMap();
		map.set("name","alisonyu");
		map.set("age","20");
		Object result = ParamAcceptor.getValue(paramMeta,map);
		if (! (result instanceof JsonObject)){
			System.out.println(result.getClass());
			assertTrue(false);
		}
		JsonObject jsonObject = (JsonObject)result;
		assertEquals("alisonyu",jsonObject.getString("name"));
		assertSame("20",jsonObject.getString("age"));
	}

	@Test
	public void testJavaBean(){
		ParamMeta paramMeta = new ParamMeta("javaBean", Person.class,null);
		paramMeta.setParamType(ParamType.BODY_PARAM);
		MultiMap map = MultiMap.caseInsensitiveMultiMap();
		map.set("name","alisonyu");
		map.set("age","20");
		map.set("id","123456789");
		Object result = ParamAcceptor.getValue(paramMeta,map);
		if (! (result instanceof Person) ){
			assertTrue(false);
		}
		Person person = (Person) result;
		assertSame(20,person.getAge());
		assertEquals("alisonyu",person.getName());

	}





}