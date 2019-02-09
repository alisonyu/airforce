package com.alisonyu.airforce.core;

import com.alisonyu.airforce.microservice.utils.ServiceMessageCodec;
import com.alisonyu.airforce.web.transfer.UnsafeLocalMessageCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class EventbusContext {

    private Vertx vertx;
    private EventBus eb;
    private volatile boolean inited = false;

    public EventbusContext(Vertx vertx){

        this.vertx = vertx;
        this.eb = vertx.eventBus();
    }

    public void init(){
        if (!inited){
            synchronized (this){
                if (inited){
                    return;
                }
                eb.registerCodec(new UnsafeLocalMessageCodec());
                eb.registerCodec(new ServiceMessageCodec());
                this.inited = true;
            }
        }
    }


}
