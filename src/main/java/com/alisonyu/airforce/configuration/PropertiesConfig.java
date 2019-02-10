package com.alisonyu.airforce.configuration;

import java.util.Optional;
import java.util.Properties;

public class PropertiesConfig implements Config {

    Properties properties = new Properties();

    public PropertiesConfig(Properties properties){
        this.properties = properties;
    }

    //命令行参数 > System.getProperties() > 环境变量 > 代码配置 > 配置文件
    @Override
    public String getValue(String key) {
        return properties.getProperty(key);
    }
}
