package com.alisonyu.airforce.web.template;

import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;

import java.util.function.Function;

public class TemplateRegistry {

    private Function<Vertx,TemplateEngine> templateEngineFactory;
    private String suffix;
    private boolean isDefault;

    public TemplateRegistry(Function<Vertx, TemplateEngine> templateEngineFactory, String suffix, boolean isDefault) {
        this.templateEngineFactory = templateEngineFactory;
        this.suffix = suffix;
        this.isDefault = isDefault;
    }


    public Function<Vertx, TemplateEngine> getTemplateEngineFactory() {
        return templateEngineFactory;
    }

    public void setTemplateEngineFactory(Function<Vertx, TemplateEngine> templateEngineFactory) {
        this.templateEngineFactory = templateEngineFactory;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
