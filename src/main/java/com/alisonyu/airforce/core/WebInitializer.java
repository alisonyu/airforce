package com.alisonyu.airforce.core;

import com.alisonyu.airforce.configuration.AirForceDefaultConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.web.HttpServerVerticle;
import com.alisonyu.airforce.web.router.mounter.RouterMounter;
import com.alisonyu.airforce.web.router.mounter.StaticRouteMounter;
import com.alisonyu.airforce.web.router.mounter.WebRouteMounter;
import com.alisonyu.airforce.web.template.HtmlTemplateEngine;
import com.alisonyu.airforce.web.router.*;
import com.alisonyu.airforce.common.tool.async.AsyncHelper;
import com.alisonyu.airforce.common.tool.TimeMeter;
import com.alisonyu.airforce.common.tool.instance.Instance;
import com.alisonyu.airforce.web.config.StaticConfiguration;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.impl.TemplateHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * init web
 */
public class WebInitializer {

    private Logger logger = LoggerFactory.getLogger(WebInitializer.class);
    private Vertx vertx;
    private List<RouterMounter> routerMounters = Collections.emptyList();
    private Set<Class<? extends AirForceVerticle>> restVerticleClazz = Collections.emptySet();
    private Function<Class<? extends AirForceVerticle>, AirForceVerticle> factory = Instance::instance;
    private RouterManager routerManager;
    private boolean isWeb = false;


    public WebInitializer(Vertx vertx){
        this.vertx = vertx;
    }


    private void initRouterManager(List<RouterMounter> routerMounters){
       RouterManager.init(vertx);
        //1、进行Web基本挂载
        RouterManager.mountRouter(new WebRouteMounter());
        //2、进行静态路由的挂载
        StaticConfiguration staticConfiguration = AirForceEnv.getConfig(StaticConfiguration.class);
        RouterManager.mountRouter(new StaticRouteMounter(staticConfiguration));
        //3、模板文件进行挂载
        RouterManager.mountRouter(router -> {
            router.
                routeWithRegex(".+\\.html")
                .order(Integer.MAX_VALUE)
                .blockingHandler(new TemplateHandlerImpl(new HtmlTemplateEngine(vertx),"template","text/html"));
        });
        //4、挂载其他Mounter
        routerMounters.forEach(mounter -> RouterManager.mountRouter(mounter));
    }

    private void deployRestVerticle(Set<Class<? extends AirForceVerticle>> restVerticleClazz,
                                   Function<Class<? extends AirForceVerticle>, AirForceVerticle> factory){
        isWeb = ! restVerticleClazz.isEmpty();
        if (!isWeb){
            return;
        }
        //deploy real verticle
        restVerticleClazz.forEach(clazz -> {
            AirForceVerticle tpl = factory.apply(clazz);
            DeploymentOptions deploymentOptions = tpl.getDeployOption();
            vertx.deployVerticle(()->{
                return factory.apply(clazz);
            },deploymentOptions);
        } );
    }

    private void startHttpServer(Vertx vertx, Router router){
        if (!isWeb){
            return;
        }
        TimeMeter timeMeter = new TimeMeter();
        timeMeter.start();
        Integer port = AirForceEnv.getConfig(AirForceDefaultConfig.SERVER_PORT,Integer.class);
        HttpServerVerticle httpServerVerticle = new HttpServerVerticle(router,port);
        try{
            String res = AsyncHelper.<String>blockingGet(handler -> vertx.deployVerticle(httpServerVerticle,handler));
            logger.info("server listen at port: {}! cost {}ms",port,timeMeter.end());
        }catch (Exception e){
            logger.error("http start error!");
        }
    }

    public void init(){
        initRouterManager(this.routerMounters);
        deployRestVerticle(this.restVerticleClazz,this.factory);
        startHttpServer(vertx,RouterManager.getRouter());
    }


    public List<RouterMounter> getRouterMounters() {
        return routerMounters;
    }

    public void setRouterMounters(List<RouterMounter> routerMounters) {
        this.routerMounters = routerMounters;
    }

    public Set<Class<? extends AirForceVerticle>> getRestVerticleClazz() {
        return restVerticleClazz;
    }

    public void setRestVerticleClazz(Set<Class<? extends AirForceVerticle>> restVerticleClazz) {
        this.restVerticleClazz = restVerticleClazz;
    }

    public Function<Class<? extends AirForceVerticle>, AirForceVerticle> getFactory() {
        return factory;
    }

    public void setFactory(Function<Class<? extends AirForceVerticle>, AirForceVerticle> factory) {
        this.factory = factory;
    }

    public RouterManager getRouterManager() {
        return routerManager;
    }

    public void setRouterManager(RouterManager routerManager) {
        this.routerManager = routerManager;
    }
}
