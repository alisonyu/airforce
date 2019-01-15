package com.alisonyu.airforce.microservice;

import com.alisonyu.airforce.microservice.service.provider.ServicePublisher;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

import java.util.List;

/**
 * init soa service
 */
public class ServiceInitializer {

    private Vertx vertx;
    private ServicePublisher servicePublisher;

    public ServiceInitializer(Vertx vertx){
        this.vertx = vertx;
        this.servicePublisher = new ServicePublisher();
    }

    public void publishServices(List<Object> services){
        services.forEach(service ->{
            Vertx deployVertx = vertx;
            if (service instanceof AbstractVerticle){
                deployVertx = ((AbstractVerticle) service).getVertx();
            }
            servicePublisher.publish(deployVertx,service);
        });
    }



}
