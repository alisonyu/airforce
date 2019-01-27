package com.alisonyu.airforce.microservice.utils;

import io.vertx.core.eventbus.DeliveryOptions;

public class ServiceMessageDeliveryOptions extends DeliveryOptions {

    public static ServiceMessageDeliveryOptions instance = new ServiceMessageDeliveryOptions();

    ServiceMessageDeliveryOptions(){
        super();
        this.setCodecName(ServiceMessageCodec.name);
    }

    public static ServiceMessageDeliveryOptions getInstance(){
        return instance;
    }



}
