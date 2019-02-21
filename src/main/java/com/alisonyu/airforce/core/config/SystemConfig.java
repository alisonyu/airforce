package com.alisonyu.airforce.core.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

@Configuration
public class SystemConfig {

    public static final String RUNMODE_STANDALONE = "standalone";
    public static final String RUNMODE_DOCKER = "docker";

    @Value("app.name")
    private String appName = "airforce";
    @Value("log.path")
    private String logPath = "/logs";
    @Value("run.mode")
    private String runMode = RUNMODE_STANDALONE;


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


    public String getRunMode(){
        return runMode;
    }

    public void setRunMode(String runMode){
        this.runMode = runMode;
    }

}
