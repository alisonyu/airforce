package com.alisonyu.airforce.web.executor;

import com.alisonyu.airforce.web.constant.http.ContentTypes;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

/**
 * serialize result depend on content-type
 */
public class Serializer {

    public static Buffer serializer(Object target,String contentType){

        //depend on type
        if (target instanceof byte[]){
            return Buffer.buffer((byte[])target);
        }
        else if (target instanceof Buffer){
            return (Buffer) target;
        }
        else if (target instanceof String || target instanceof Number){
            return Buffer.buffer(String.valueOf(target));
        }

        //depend on content-type
        if (ContentTypes.JSON.equals(contentType)){
            return Json.encodeToBuffer(target);
        }
        else{
            throw new IllegalStateException();
        }
    }


}
