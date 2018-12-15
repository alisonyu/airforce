package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.airforce.microservice.service.provider.ServiceProvider;
import com.alisonyu.airforce.microservice.service.provider.ServicePublisher;
import com.alisonyu.example.test.Person;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec;
import io.vertx.core.json.Json;

import static org.junit.Assert.*;

public class ConsumerProviderTest {


    public static void main(String[] args) {
        Vertx vertx =Vertx.vertx();
        //vertx.eventBus().(new JsonObjectMessageCodec());
        EchoService echoService = new EchoServiceImpl();
        ServicePublisher publisher = new ServicePublisher();
        publisher.publish(vertx,echoService);
        EchoService consumer = ConsumerProvider.getConsumer(vertx,EchoService.class, ServiceProvider.defaultGroup,ServiceProvider.defaultVersion);
        System.out.println(consumer.echo("hello world"));
        QueryParam param = new QueryParam("alisonyu",123);
        Person person = consumer.getPerson(param);
        System.out.println(Json.encode(person));
    }


}