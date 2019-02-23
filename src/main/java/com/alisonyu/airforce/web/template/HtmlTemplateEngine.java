package com.alisonyu.airforce.web.template;

import com.alisonyu.airforce.common.tool.async.AsyncResultHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.web.common.template.TemplateEngine;

import java.util.Map;

/**
 * 解析Html
 * @author 四昭
 * @date 2018/11/17下午4:55
 */
public class HtmlTemplateEngine implements TemplateEngine {

    private OpenOptions openOptions = new OpenOptions();
    private FileSystem fileSystem;

    public HtmlTemplateEngine(Vertx vertx){
        fileSystem = vertx.fileSystem();
    }


    @Override
    public void render(Map<String, Object> map, String s, Handler<AsyncResult<Buffer>> handler) {
        final String filePath =  s;
        fileSystem
            .open(filePath,openOptions,res->{
                if (res.succeeded()){
                    AsyncFile asyncFile =  res.result();
                    asyncFile.handler(buffer -> {
                        handler.handle(AsyncResultHelper.success(buffer));
                    });
                }else{
                    handler.handle(AsyncResultHelper.fail(res.cause()));
                }
            });
    }

    @Override
    public boolean isCachingEnabled() {
        return false;
    }


}
