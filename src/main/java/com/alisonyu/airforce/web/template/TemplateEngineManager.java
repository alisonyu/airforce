package com.alisonyu.airforce.web.template;

import io.reactivex.Flowable;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.TemplateEngine;

public interface TemplateEngineManager {

    TemplateEngineManager instance = new TemplateEngineManagerImpl();

    /**
     * 注册渲染模板处理器
     * @param engine 处理器
     * @param suffix 文件后缀，可空
     * @param isDefault 是否是默认处理器
     */
    void registerTemplate(TemplateEngine engine,String suffix,boolean isDefault);

    Flowable<Buffer> render(ModelView modelView);

    static TemplateEngineManager getInstance(){
        return instance;
    }

}
