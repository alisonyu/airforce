package com.alisonyu.airforce.message;

import io.reactivex.Flowable;
import io.vertx.core.Vertx;

public interface MessageListener {

    static MessageListener create(Vertx vertx,String topic){
        return new MessageListenerImpl(vertx,topic);
    }

    Flowable<Message> listen(String tag);

}
