package com.alisonyu.airforce.web.template;

import com.alisonyu.airforce.common.tool.async.AsyncHelper;
import io.reactivex.Flowable;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateEngineManagerImpl implements TemplateEngineManager {

    private Logger logger = LoggerFactory.getLogger(TemplateEngineManager.class);

    private Map<Class<? extends TemplateEngine>,TemplateEngine> templateEngineMap = new ConcurrentHashMap<>();

    private Map<String,TemplateEngine> suffixToEngineMap = new ConcurrentHashMap<>();

    private TemplateEngine defaultEngine;

    @Override
    public void registerTemplate(TemplateEngine engine, String suffix,boolean isDefault) {
        if (suffix!=null){
            suffixToEngineMap.put(suffix,engine);
        }
        templateEngineMap.put(engine.getClass(),engine);
        if (isDefault){
            synchronized (this){
                defaultEngine = engine;
            }
        }
    }


    @Override
    public Flowable<Buffer> render(ModelView modelView) {
        JsonObject data = modelView.getData() == null ? new JsonObject() : modelView.getData();
        String fileName = modelView.getFileName();
        Objects.requireNonNull(fileName,"template fileName is required");

        //determine tpl type by engine type
        if (modelView.getEngine() != null){
            Class<? extends TemplateEngine> type = modelView.getEngine();
            TemplateEngine engine = templateEngineMap.values().stream().filter(o -> type.isAssignableFrom(o.getClass())).findFirst().orElse(null);
            if (engine == null){
                throw new IllegalArgumentException(type.getName()+" is not registered");
            }

            return renderInterval(fileName,data,engine);
        }
        //determine tpl type by file suffix
        else{
            //fixme
            String suffix = fileName.substring(fileName.lastIndexOf('.'),fileName.length());
            TemplateEngine engine = suffixToEngineMap.get(suffix);
            if (engine != null){
                return renderInterval(fileName,data,engine);
            }else if (defaultEngine != null){
                return renderInterval(fileName,data,defaultEngine);
            }else{
                throw new IllegalStateException("no valid templateEngine to process "+fileName);
            }
        }

    }

    private Flowable<Buffer> renderInterval(String fileName,JsonObject data,TemplateEngine engine){
        return AsyncHelper.fromAsyncResult(as -> engine.render(data,fileName,as));
    }


}
