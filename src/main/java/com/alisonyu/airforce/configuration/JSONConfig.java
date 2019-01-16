package com.alisonyu.airforce.configuration;

import io.vertx.core.json.JsonObject;

public class JSONConfig implements Config {

    JsonObject config = new JsonObject();

    public JSONConfig(JsonObject jsonObject){
        this.config = jsonObject;
    }

    @Override
    public String getValue(String key) {
        return getConfigValue(key);
    }


    private String getConfigValue(String expr){
        if (expr == null || "".equals(expr)){
            return null;
        }
        String[] in = expr.split("\\.");
        if (in.length == 1){
            String key = in[0];
            return config.getString(key);
        }
        else{
            JsonObject node = config;
            for (int i = 0;i<in.length-1;i++){
                node = node.getJsonObject(in[i]);
                if (node == null) {
                    return null;
                }
            }
            return node.getString(in[in.length-1]);
        }
    }


}
