package com.alisonyu.airforce.configuration;

import java.util.Optional;

public class SystemConfig implements Config {

    @Override
    public String getValue(String key) {
        final String ENV_KEY = key.replaceAll("\\.","_").toUpperCase();
        return Optional.ofNullable(System.getProperty(key))
                    .orElse(System.getenv(ENV_KEY));
    }
}
