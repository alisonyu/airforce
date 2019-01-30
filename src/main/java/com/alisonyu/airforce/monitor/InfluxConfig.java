package com.alisonyu.airforce.monitor;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

@Configuration(prefix = "metric.influx")
public class InfluxConfig {

    @Value("enable")
    private boolean enable = false;
    @Value("url")
    private String url;
    @Value("db")
    private String db;
    @Value("username")
    private String username;
    @Value("password")
    private String password;

    public boolean isEnable() {
        return enable;
    }

    public String getUrl() {
        return url;
    }

    public String getDb() {
        return db;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
