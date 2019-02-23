package com.alisonyu.airforce.core;

import com.alisonyu.airforce.web.router.RouterManager;
import io.vertx.core.*;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AirForceContext {

    private static Logger logger = LoggerFactory.getLogger(AirForceContext.class);
    private Vertx vertx;
    private Queue<Supplier<AbstractVerticle>> deferDeployQueue = new ConcurrentLinkedQueue<>();

    public void queueVerticleInternal(Supplier<AbstractVerticle> supplier){
        deferDeployQueue.offer(supplier);
    }

    public void deployVerticle(Supplier<AbstractVerticle> supplier,DeploymentOptions deploymentOptions, Handler<AsyncResult<String>> completionHandler){
        AbstractVerticle tpl = supplier.get();
        if (deploymentOptions == null){
            if (tpl instanceof AirForceVerticle){
                deploymentOptions = ((AirForceVerticle) tpl).getDeployOption();
            }else{
                deploymentOptions = new DeploymentOptions();
            }
        }
        vertx.deployVerticle(supplier::get,deploymentOptions,completionHandler);
    }

    public void publishServices(List<Object> services){
        List<Object> processService = services.stream()
                .map(service -> {
                    if (service instanceof AirForceVerticle){
                        queueVerticleInternal(()-> (AbstractVerticle) service);
                        logger.warn("{} will be deploy as verticle instead of service",service.getClass());
                        return null;
                    }else{
                        return service;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        ServiceInitializer publisher = new ServiceInitializer(vertx);
        publisher.publishServices(processService);
    }

    void start(Vertx vertx){
        this.vertx = vertx;
        deployQueuedVerticles();
    }

    private void deployQueuedVerticles(){
        while(deferDeployQueue.peek() != null){
            Supplier<AbstractVerticle> supplier = deferDeployQueue.poll();
            deployVerticle(supplier,null,null);
        }
    }

    public Vertx getVertx(){
        return this.vertx;
    }

    public Router getRouter(){
        return RouterManager.getRouter();
    }




}
