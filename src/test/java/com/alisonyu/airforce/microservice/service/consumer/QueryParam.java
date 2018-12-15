package com.alisonyu.airforce.microservice.service.consumer;

public class QueryParam {

    private String name;
    private Integer age;

    public QueryParam(){

    }

    public QueryParam(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
