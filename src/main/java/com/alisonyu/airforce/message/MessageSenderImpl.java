package com.alisonyu.airforce.message;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;


public class MessageSenderImpl implements MessageSender {

    private final Vertx vertx;
    private final String topic;

    public MessageSenderImpl(Vertx vertx,String topic){
        this.vertx = vertx;
        this.topic = topic;
    }

    @Override
    public void publish(String tag, Buffer payload) {
        vertx.eventBus().publish(this.topic+":"+tag,payload);
    }

    @Override
    public void send(String tag, Buffer payload) {
        vertx.eventBus().send(this.topic+":"+tag,payload);
    }
}
