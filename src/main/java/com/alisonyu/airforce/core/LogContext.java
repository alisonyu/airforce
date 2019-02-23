package com.alisonyu.airforce.core;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.configuration.CommandLineConfig;
import com.alisonyu.airforce.core.config.SystemConfig;
import org.slf4j.LoggerFactory;

public class LogContext {

    private static CommandLineConfig config;
    private static final String APP_NAME = "app.name";
    private static final String DEFAULT_APP_NAME  = "airforce";
    private static final String LOG_PATH = "log.path";
    private static final String DEFAULT_LOG_PATH = "/logs";

    public static void init(String[] args){
        //config = new CommandLineConfig(args);
        SystemConfig config = AirForceEnv.getConfig(SystemConfig.class);
        if (System.getProperty(APP_NAME) == null){
            System.setProperty(APP_NAME,config.getAppName());
        }
        if (System.getProperty(LOG_PATH) == null){
            System.setProperty(LOG_PATH,config.getLogPath());
        }

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (lc != null && lc.getLoggerList().size() > 0){
            lc.getStatusManager().clear();
            lc.reset();
            ContextInitializer ci  = new ContextInitializer(lc);
            try {
                ci.autoConfig();
            } catch (JoranException e) {
                e.printStackTrace();
            }
        }
        StatusPrinter.print(lc);


    }

    private static String getValue(String key,String defaultValue){
        String value = config.getValue(key);
        if (value == null) value = System.getProperty(key);
        if (value == null) value = System.getenv(key);
        if (value == null) value = defaultValue;
        return value;
    }


}
