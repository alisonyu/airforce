package com.alisonyu.airforce.monitor;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

@Configuration(prefix = "metric.dropWizard")
public class DropWizardConfig {

    @Value("enable")
    private boolean enable = false;

    @Value("jms.enable")
    private boolean enableJms = false;

    @Value("jms.domain")
    private String JmsDomain = "airforce";

    public boolean isEnable() {
        return enable;
    }

    public boolean isEnableJms() {
        return enableJms;
    }

    public String getJmsDomain() {
        return JmsDomain;
    }
}
