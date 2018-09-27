package com.alisonyu.example.test;

import com.alisonyu.airforce.microservice.RestVerticle;
import com.alisonyu.airforce.microservice.anno.BodyParam;
import com.alisonyu.airforce.microservice.anno.Sync;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuzhiyi
 * @date 2018/9/12 10:17
 */
@Path("hello")
public class HelloRestVerticle extends RestVerticle{

	private int counter = 1;


	//注意，这样是线程安全的
	@GET
	@Path("hello")
	public String hello(@QueryParam("name")String name){
		counter++;
		return name + ":" + counter;
	}

	@GET
	@Path("hi")
	public String hi(){
		return "hello world";
	}

	@POST
	@Path("json")
	public JsonObject json(@QueryParam("name") String name,
						   @FormParam("age")Integer age,
						   @CookieParam("cookie")String cookie,
						   @HeaderParam(HttpHeaders.CONTENT_TYPE)String contentType){
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("name",name)
				.put("age",age)
				.put("cookie",cookie)
				.put("content-type",contentType);
		return jsonObject;
	}

	@GET
	@Path("user/:name")
	public String getUserName(@PathParam("name") String name){
		return name;
	}


	@POST
	@Sync
	@Path("blocking")
	public String blocking() throws InterruptedException {
		Thread.sleep(2000);
		return "hello world";
	}

	@Path("error")
	@GET
	public String error(){
		throw new RuntimeException("wow");
	}

	@Path("thread")
	@GET
	public String thread(){
		return Thread.currentThread().getName();
	}


	@Path("jsonBody")
	@GET
	public JsonObject body(@BodyParam JsonObject params){
		return params;
	}

	@Path("person")
	@GET
	public Person person(@BodyParam Person person){
		return person;
	}



	@Path("array")
	@GET
	public Person[] arr(){
		Person person = new Person();
		person.setAge(66);
		person.setName("arr");
		Person[] arr;

		return new Person[]{person,person};
	}

	@Path("map")
	@GET
	public Map testMap(){
		HashMap<String,Object> map = new HashMap<>();
		map.put("name","yuzhiyi");
		map.put("age",22);
		map.put("school","scau");
		return map;
	}

	@Path("stringFuture")
	@GET
	public Future<String> stringFuture(){
		Future<String> future = Future.future();
		future.complete("hahaha");
		return future;
	}





}
