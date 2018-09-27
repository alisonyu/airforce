package com.alisonyu.mock;

import io.vertx.ext.web.RoutingContext;
import static org.mockito.Mockito.*;

/**
 * @author yuzhiyi
 * @date 2018/9/27 22:43
 */
public class MockRoutingContextBuilder {

	private RoutingContext context = mock(RoutingContext.class);


	private MockRoutingContextBuilder(){

	}

	public static MockRoutingContextBuilder builder(){
		return new MockRoutingContextBuilder();
	}

	public void addHeader(String header,String value){

	}

	public void addQueryParams(String key,Object value){

	}

	public void addSession(String key,Object value){

	}

	public void addCookie(String key,String value){

	}



	public RoutingContext build(){
		return null;
	}





}
