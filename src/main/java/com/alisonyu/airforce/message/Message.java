package com.alisonyu.airforce.message;

import io.vertx.core.buffer.Buffer;

public class Message {

    private String topic;
    private String tag;
    private Buffer payload;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Buffer getPayload() {
        return payload;
    }

    public void setPayload(Buffer payload) {
        this.payload = payload;
    }
}
