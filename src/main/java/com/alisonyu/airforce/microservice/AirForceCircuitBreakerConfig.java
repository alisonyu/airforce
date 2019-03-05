package com.alisonyu.airforce.microservice;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

public class AirForceCircuitBreakerConfig {

    private volatile CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();

    public void setCircuitBreakerConfig(CircuitBreakerConfig circuitBreakerConfig){
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    public CircuitBreakerConfig getCircuitBreakerConfig(){
        return this.circuitBreakerConfig;
    }



}
