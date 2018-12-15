package com.alisonyu.airforce.microservice.service.provider;

import com.alisonyu.airforce.tool.instance.Instance;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 服务方法代理类
 */
public class ServiceMethodProxy {


    private Object target;
    private Method proxyMethod;
    private DeliveryOptions deliveryOptions;

    public ServiceMethodProxy(Object instance, Method method){
        this.target = instance;
        this.proxyMethod = method;
        this.deliveryOptions = new DeliveryOptions();
    }


    public void call(Message<String> message){
        Object result = null;
        try{
            String json = message.body();
            JsonObject jsonObject = new JsonObject(json);
            JsonArray params = jsonObject.getJsonArray("params");
            Class<?>[] typeClasses = proxyMethod.getParameterTypes();
            Type[] types = proxyMethod.getGenericParameterTypes();
            if (params.size() != typeClasses.length){
                throw new IllegalArgumentException("参数数量不统一");
            }
            Object[] callParams = new Object[params.size()];
            for (int i=0;i<params.size();i++){
                Object param = Instance.cast(params.getValue(i),types[i],typeClasses[i]);
                callParams[i] = param;
            }
            result = proxyMethod.invoke(target,callParams);
        }catch (Throwable e){
            e.printStackTrace();
            result = e;
        }
        ServiceResult serviceResult = new ServiceResult(result);
        JsonObject jsonResult  = JsonObject.mapFrom(serviceResult);
        message.reply(jsonResult,deliveryOptions);
    }


}
