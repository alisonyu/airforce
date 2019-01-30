package com.alisonyu.airforce.common.tool;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SerializeUtils {

    private static Logger logger = LoggerFactory.getLogger(SerializeUtils.class);

    public static String toJsonString(Object in){
        String out;
        if (in instanceof JsonObject || in instanceof JsonArray){
            out = in.toString();
        }
        else if (List.class.isAssignableFrom(in.getClass())){
            out = new JsonArray((List) in).toString();
        }
        else if (in.getClass().isArray()){
            Object[] arr = (Object[]) in;
            out = new JsonArray(Arrays.asList(arr)).toString();
        }
        else if (in instanceof Number){
            out = in.toString();
        }
        else if (in instanceof String){
            return String.valueOf(in);
        }
        else{
            try{
                out = JsonObject.mapFrom(in).toString();
            }catch (Exception e){
                logger.error(e.getMessage());
                return in.toString();
            }
        }
        return out;
    }

}
