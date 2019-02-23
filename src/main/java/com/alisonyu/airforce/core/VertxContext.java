package com.alisonyu.airforce.core;

import com.alisonyu.airforce.common.tool.async.AsyncHelper;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * airforce's vertx manager
 */
public class VertxContext {

    private VertxOptions vertxOptions;
    private Vertx vertx;
    private volatile boolean inited = false;

    public VertxContext(Vertx vertx){
        this.vertx = vertx;
    }

    public VertxContext(VertxOptions vertxOptions){
        this.vertxOptions = vertxOptions;
    }

    public VertxContext(){

    }


    public void init(){
        if (!inited){
            synchronized (this){
                if (inited){
                    return;
                }
                if (this.vertx == null){
                    if (this.vertxOptions == null){
                        this.vertxOptions = new VertxOptions();
                    }
                    //init vertx
                    if (vertxOptions.getClusterManager() != null){
                        this.vertx = AsyncHelper.blockingGet(handler-> Vertx.clusteredVertx(vertxOptions,handler));
                    }else{
                        this.vertx = Vertx.vertx(vertxOptions);
                    }
                }
                this.inited = true;
            }
        }
    }

    public Vertx getVertx(){
        ensureInited();
        return this.vertx;
    }

    private void ensureInited(){
        if (!inited){
            throw new IllegalStateException("vertx has not benn inited");
        }
    }

}
