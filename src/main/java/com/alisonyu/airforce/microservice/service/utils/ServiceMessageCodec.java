package com.alisonyu.airforce.microservice.service.utils;

import com.google.common.collect.Lists;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.nustaq.serialization.FSTConfiguration;

import java.util.List;

public class ServiceMessageCodec implements MessageCodec<Object,Object> {

    public static final String name = ServiceMessageCodec.class.getName();

    static FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

    @Override
    public void encodeToWire(Buffer buffer, Object s) {
        byte[] bytes = configuration.asByteArray(s);
        buffer.appendInt(bytes.length).appendBytes(bytes);
    }

    @Override
    public Object decodeFromWire(int pos, Buffer buffer) {
        //read length
        int length = buffer.getInt(pos);
        pos += 4;
        //read content
        byte[] bytes = buffer.getBytes(pos,pos+length);
        return configuration.asObject(bytes);
    }

    /**
     * if sent locally,transfer cloned object
     */
    @Override
    public Object transform(Object s) {
        byte[] bytes = configuration.asByteArray(s);
        return configuration.asObject(bytes);
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
