package com.alisonyu.airforce.microservice.provider;

import io.vertx.core.json.Json;

/**
 * 服务调用类
 */
public class ServiceResult {

    /**
     * 使用JSON进行序列化
     */
    private String serializeResult;
    /**
     * 被序列化的类的类型
     */
    private String clazz;

    public ServiceResult(){}

    public ServiceResult(Object result){
        if (result == null){
            clazz = Object.class.getName();
        }else{
            clazz = result.getClass().getName();
        }
        serializeResult = Json.encode(result);
    }

    public String getSerializeResult() {
        return serializeResult;
    }

    public void setSerializeResult(String serializeResult) {
        this.serializeResult = serializeResult;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
