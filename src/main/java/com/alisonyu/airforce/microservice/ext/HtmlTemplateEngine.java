package com.alisonyu.airforce.microservice.ext;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;
import io.vertx.reactivex.FlowableHelper;

import java.util.Map;

/**
 * 解析Html
 * @author 四昭
 * @date 2018/11/17下午4:55
 */
public class HtmlTemplateEngine implements TemplateEngine {

    private OpenOptions openOptions = new OpenOptions();

    @Override
    public void render(RoutingContext context, String templateDirectory, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
        String filePath = templateDirectory + "/" + templateFileName;
        HttpServerResponse resp = context.response();
        resp.putHeader("content-type","text/html");
        resp.setChunked(true);
        context.vertx().fileSystem()
                .open(filePath,openOptions,res->{
                    if (res.succeeded()){
                        AsyncFile asyncFile =  res.result();
                        FlowableHelper.toFlowable(asyncFile)
                                .doOnComplete(()->{
                                    resp.end();
                                    asyncFile.close();
                                })
                                .subscribe(resp::write);
                    }
                });

    }


}
