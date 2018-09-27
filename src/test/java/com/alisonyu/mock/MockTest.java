package com.alisonyu.mock;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
/**
 * @author yuzhiyi
 * @date 2018/9/27 22:26
 */
public class MockTest {

	static MockContext context;

	@BeforeClass
	public static void testMock(){
		MockContext context = mock(MockContext.class);
		when(context.getUrl()).thenReturn("http://127.0.0.1");
		Map<String,Object> params = new HashMap<>();
		params.put("name","alisonyu");
		params.put("age",22);
		when(context.getParams()).thenReturn(Collections.unmodifiableMap(params));
		MockTest.context = context;
	}

	@Test
	public void testUrl(){
		assertEquals("http://127.0.0.1",context.getUrl());
	}

	@Test
	public void testParam(){
		assertEquals("alisonyu",context.getParams().get("name"));
		assertEquals(22,context.getParams().get("age"));
	}


}
