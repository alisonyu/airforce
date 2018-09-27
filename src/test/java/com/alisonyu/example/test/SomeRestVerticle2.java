package com.alisonyu.example.test;

import com.alisonyu.airforce.microservice.RestVerticle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author yuzhiyi
 * @date 2018/9/14 16:12
 */
@Path("some2")
public class SomeRestVerticle2 extends RestVerticle{

	@Path("thread")
	@GET
	public String thread(){
		return Thread.currentThread().getName();
	}

}