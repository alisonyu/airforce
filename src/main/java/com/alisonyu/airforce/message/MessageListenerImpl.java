package com.alisonyu.airforce.message;

import io.reactivex.Flowable;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;

public class MessageListenerImpl implements MessageListener {

    private Vertx vertx;
    private String topic;

    public MessageListenerImpl(Vertx vertx,String topic){
        this.vertx = vertx;
        this.topic = topic;
    }


    @Override
    public Flowable<Message> listen(String tag) {
        MessageConsumer<Buffer> consumer = vertx.eventBus().consumer(this.topic+":"+tag);
        return Flowable.<Message>fromPublisher(publisher -> {
            consumer.handler(msg -> {
                Message message = new Message();
                message.setTag(tag);
                message.setTag(topic);
                message.setPayload(msg.body());

                publisher.onNext(message);

            });
        })
        //unregister when stop listen
        .doFinally(()-> {
            consumer.unregister();
        });
    }




}
