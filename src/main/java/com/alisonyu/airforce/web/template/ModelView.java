package com.alisonyu.airforce.web.template;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.TemplateEngine;

public class ModelView {

    private String fileName;
    private Class<? extends TemplateEngine> engine;
    private JsonObject data;


    public ModelView templateName(String s){
        this.fileName = s;
        return this;
    }

    public ModelView engine(Class<? extends TemplateEngine> engine){
        this.engine = engine;
        return this;
    }

    public ModelView data(JsonObject data){
        this.data = data;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public Class<? extends TemplateEngine> getEngine() {
        return engine;
    }

    public JsonObject getData() {
        return data;
    }
}
