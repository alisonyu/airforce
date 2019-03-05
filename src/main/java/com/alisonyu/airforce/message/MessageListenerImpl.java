package com.alisonyu.airforce.message;

import io.reactivex.Flowable;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;

public class MessageListenerImpl implements MessageListener {

    private Vertx vertx;
    private String topic;

    private Flowable<Message> messageSource;

    public MessageListenerImpl(Vertx vertx,String topic){
        this.vertx = vertx;
        this.topic = topic;
    }


    @Override
    public Flowable<Message> listen(String tag) {
        if (messageSource == null){
            synchronized (this){
                MessageConsumer<Buffer> consumer = vertx.eventBus().consumer(this.topic+":"+tag);
                this.messageSource = Flowable.<Message>fromPublisher(publisher -> {
                    consumer.handler(msg -> {
                        Message message = new Message();
                        message.setTag(tag);
                        message.setTag(topic);
                        message.setPayload(msg.body());
                        publisher.onNext(message);
                    });
                }).publish();
            }
        }
        return messageSource;

    }

    @Override
    public Flowable<Message> listenOnce(String tag) {
        MessageConsumer<Buffer> consumer = vertx.eventBus().consumer(this.topic+":"+tag);
        return Flowable.<Message>fromPublisher(publisher -> {
            consumer.handler(msg -> {
                Message message = new Message();
                message.setTag(tag);
                message.setTag(topic);
                message.setPayload(msg.body());
                publisher.onNext(message);
                publisher.onComplete();
                consumer.unregister();
            });
        });
    }


}
