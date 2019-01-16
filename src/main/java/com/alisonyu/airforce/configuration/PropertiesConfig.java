package com.alisonyu.airforce.configuration;

import java.util.Properties;

public class PropertiesConfig implements Config {

    Properties properties = new Properties();

    public PropertiesConfig(Properties properties){
        this.properties = properties;
    }


    @Override
    public String getValue(String key) {
        return properties.getProperty(key);
    }
}
