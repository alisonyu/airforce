package com.alisonyu.airforce.core.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

@Configuration
public class SystemConfig {

    @Value("app.name")
    private String appName = "airforce";
    @Value("log.path")
    private String logPath = "/logs";


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
