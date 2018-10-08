package com.alisonyu.example.test;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * @author yuzhiyi
 * @date 2018/10/8 13:11
 */
@Component
public class SimpleService {

	public String hello(String name){
		return "hello "+name;
	}

}
