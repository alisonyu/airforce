package com.alisonyu.airforce.message;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;


public interface MessageSender {

    static MessageSender create(Vertx vertx, String topic){
        return new MessageSenderImpl(vertx,topic);
    }

    void publish(String tag, Buffer payload);

    void send(String tag,Buffer payload);



}
