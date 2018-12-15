package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.example.test.Person;

public interface EchoService {

    public String echo(String content);

    public Person getPerson(QueryParam queryParam);


}
