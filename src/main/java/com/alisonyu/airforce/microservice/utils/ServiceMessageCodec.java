package com.alisonyu.airforce.microservice.utils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.nustaq.serialization.FSTConfiguration;

public class ServiceMessageCodec implements MessageCodec<Object,Object> {

    public static final String name = ServiceMessageCodec.class.getName();

    static ThreadLocal<FSTConfiguration> configuration = new ThreadLocal<FSTConfiguration>(){
        @Override
        protected FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }
    };

    @Override
    public void encodeToWire(Buffer buffer, Object s) {
        byte[] bytes = configuration.get().asByteArray(s);
        buffer.appendInt(bytes.length).appendBytes(bytes);
    }

    @Override
    public Object decodeFromWire(int pos, Buffer buffer) {
        //read length
        int length = buffer.getInt(pos);
        pos += 4;
        //read content
        byte[] bytes = buffer.getBytes(pos,pos+length);
        return configuration.get().asObject(bytes);
    }

    /**
     * if sent locally,transfer cloned object
     */
    @Override
    public Object transform(Object s) {
        byte[] bytes = configuration.get().asByteArray(s);
        return configuration.get().asObject(bytes);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
