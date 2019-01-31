package com.alisonyu.airforce.web.template;

import io.reactivex.Flowable;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.TemplateEngine;

public interface TemplateEngineManager {

    void registerTemplate(Class<? extends TemplateEngine> engine,String fileType);

    void registerTemplate(TemplateEngine engine,String fileType);

    Flowable<Buffer> render(ModelView modelView);

}
