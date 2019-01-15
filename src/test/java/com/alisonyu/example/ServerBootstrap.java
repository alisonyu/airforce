package com.alisonyu.example;

import com.alisonyu.airforce.AirForceApplication;
import com.alisonyu.airforce.cloud.config.anno.EnableAirforceCloud;


/**
 * @author yuzhiyi
 * @date 2018/9/17 15:04
 */
//@EnableAirforceCloud
public class ServerBootstrap {

	public static void main(String[] args){
		AirForceApplication.run(ServerBootstrap.class,args);
	}

}
