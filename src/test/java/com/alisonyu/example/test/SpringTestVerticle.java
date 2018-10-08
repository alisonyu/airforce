package com.alisonyu.example.test;

import com.alisonyu.airforce.microservice.AbstractRestVerticle;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * @author yuzhiyi
 * @date 2018/10/8 13:12
 */
@Path("spring")
public class SpringTestVerticle extends AbstractRestVerticle {


	@Autowired
	private SimpleService simpleService;

	@Path("test")
	@GET
	public String test(@QueryParam("name") String name){
		return simpleService.hello(name);
	}

	@Path("test2")
	@GET
	public String test2(){
		return "spring";
	}



}
