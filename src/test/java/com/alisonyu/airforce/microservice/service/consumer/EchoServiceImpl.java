package com.alisonyu.airforce.microservice.service.consumer;

import com.alisonyu.example.test.Person;

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


}
