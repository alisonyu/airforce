package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.example.test.Person;

import java.util.List;

public interface EchoService {

    public String echo(String content);

    public Person getPerson(QueryParam queryParam);

    public List<Person> getPeople();

    public void hello();

}
