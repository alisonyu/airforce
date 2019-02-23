package com.alisonyu.airforce.web.config;

import com.alisonyu.airforce.configuration.anno.Value;


public class HttpServerConfig {

    @Value("server.port")
    private Integer port = 9090;

    public Integer getPort() {
        return port;
    }

}
