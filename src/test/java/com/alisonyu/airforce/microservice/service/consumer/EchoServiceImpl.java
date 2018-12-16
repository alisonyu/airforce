package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.example.test.Person;
import com.google.common.collect.Lists;

import java.util.List;

public class EchoServiceImpl implements EchoService {


    @Override
    public String echo(String content) {
        return content;
    }

    public Person getPerson(QueryParam queryParam){
        Person person = new Person();
        person.setAge(queryParam.getAge());
        person.setName(queryParam.getName());
        return person;
    }

    @Override
    public List<Person> getPeople() {
        Person person = new Person();
        person.setName("aaa");
        person.setAge(123);
        return Lists.newArrayList(person);
    }

    @Override
    public void hello() {
        System.out.println("hello");
    }
}
