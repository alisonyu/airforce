package com.alisonyu.airforce.configuration;

public class EmptyConfig implements Config{
    @Override
    public String getValue(String key) {
        return null;
    }
}
