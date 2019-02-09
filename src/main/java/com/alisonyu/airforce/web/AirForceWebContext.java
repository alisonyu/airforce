package com.alisonyu.airforce.web;

import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.core.AirForceContext;
import com.alisonyu.airforce.web.config.StaticConfiguration;
import com.alisonyu.airforce.web.exception.ExceptionHandler;
import com.alisonyu.airforce.web.exception.ExceptionManager;
import com.alisonyu.airforce.web.router.RouterManager;
import com.alisonyu.airforce.web.router.mounter.RouterMounter;
import com.alisonyu.airforce.web.router.mounter.StaticRouteMounter;
import com.alisonyu.airforce.web.router.mounter.WebRouteMounter;
import com.alisonyu.airforce.web.template.TemplateEngineManager;
import com.alisonyu.airforce.web.template.TemplateRegistry;
import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

import java.util.List;

public class AirForceWebContext {

    public AirForceWebContext(Vertx vertx,
                              AirForceContext airForceContext,
                              Router router,
                              List<RouterMounter> routerMounters,
                              List<TemplateRegistry> templateRegistries,
                              List<ExceptionHandler> exceptionHandlers,
                              boolean embbeddedHttpServer){

        //init router Manager
        if (router != null){
            RouterManager.init(router);
        }else{
            router = Router.router(vertx);
            RouterManager.init(Router.router(vertx));
            RouterManager.mountRouter(new WebRouteMounter());
            RouterManager.mountRouter(new StaticRouteMounter(AirForceEnv.getConfig(StaticConfiguration.class)));
        }


        if (routerMounters != null){
            routerMounters.forEach(routerMounter ->  RouterManager.mountRouter(routerMounter));
        }

        if (exceptionHandlers != null){
            exceptionHandlers.forEach(handler -> ExceptionManager.registerExceptionHandler(handler));
        }

        if (templateRegistries != null){
            templateRegistries.forEach(templateRegistry -> {
                TemplateEngine engine = templateRegistry.getTemplateEngineFactory().apply(vertx);
                TemplateEngineManager.getInstance().registerTemplate(engine,templateRegistry.getSuffix(),templateRegistry.isDefault());
            });
        }else{
            TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);
            TemplateEngineManager.getInstance().registerTemplate(engine,".html",true);
        }

        //deploy http server
        if (embbeddedHttpServer){
           airForceContext.queueVerticleInternal( ()-> {
               return new HttpServerVerticle();
           } );
        }
    }


    public Router getRouter(){
        return RouterManager.getRouter();
    }


}
