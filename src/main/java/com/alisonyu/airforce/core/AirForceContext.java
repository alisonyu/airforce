package com.alisonyu.airforce.core;

import com.alisonyu.airforce.web.router.RouterManager;
import io.vertx.core.*;
import io.vertx.ext.web.Router;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class AirForceContext {

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



    void start(Vertx vertx){
        this.vertx = vertx;
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
