package com.alisonyu.example.test;

import com.alisonyu.airforce.microservice.AirForceVerticle;
import io.vertx.core.DeploymentOptions;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author yuzhiyi
 * @date 2018/9/14 16:05
 */
@Path("some")
public class SomeRestVerticle extends AirForceVerticle {

	@Path("thread")
	@GET
	public String thread(){
		return Thread.currentThread().getName();
	}

	@Override
	public DeploymentOptions getDeployOption() {
		DeploymentOptions deploymentOptions = new DeploymentOptions();
		deploymentOptions.setInstances(4);
		return deploymentOptions;
	}
}
