package com.alisonyu.spring;

import com.alisonyu.airforce.tool.TimeMeter;
import io.vertx.core.Vertx;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author yuzhiyi
 * @date 2018/10/7 17:57
 */
@SpringBootConfiguration
@ComponentScan
public class SpringTestConfiguration {

	public static void main(String[] args){
		Vertx vertx = Vertx.vertx();
		TimeMeter timeMeter = new TimeMeter();
		timeMeter.start();
		GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
		genericApplicationContext.getBeanFactory().registerSingleton(Vertx.class.getCanonicalName(),vertx);
		genericApplicationContext.refresh();
		SpringApplicationBuilder builder = new SpringApplicationBuilder(SpringTestConfiguration.class);
		builder.parent(genericApplicationContext);
		//ConfigurableApplicationContext configurableApplicationContext = builder.context();
		builder.web(WebApplicationType.NONE);
		ConfigurableApplicationContext context = builder.run(args);
		Vertx bean = context.getBean(Vertx.class);
		TestService testService = context.getBean(TestService.class);
		assert bean == vertx;
		System.out.println(testService.hello());
		System.out.println("启动spring使用了"+timeMeter.end()+"ms");
	}

}
