package com.alisonyu.airforce.microservice;

import com.alisonyu.airforce.configuration.AirForceDefaultConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.microservice.ext.HtmlTemplateEngine;
import com.alisonyu.airforce.microservice.router.*;
import com.alisonyu.airforce.tool.TimeMeter;
import com.alisonyu.airforce.tool.instance.Instance;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.impl.TemplateHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * init web
 */
public class WebInitializer {

    private Logger logger = LoggerFactory.getLogger(WebInitializer.class);
    private Vertx vertx;
    private List<RouterMounter> routerMounters = Collections.emptyList();
    private Set<Class<? extends AbstractRestVerticle>> restVerticleClazz = Collections.emptySet();
    private Function<Class<? extends AbstractRestVerticle>,AbstractRestVerticle> factory = Instance::instance;
    private RouterManager routerManager;
    private boolean isWeb = false;


    public WebInitializer(Vertx vertx){
        this.vertx = vertx;
    }


    private void initRouterManager(List<RouterMounter> routerMounters){
        RouterManager routerManager = new RouterManager(vertx);
        //1、进行Web基本挂载
        routerManager.doMount(new WebRouteMounter());
        //2、进行静态路由的挂载
        StaticConfiguration staticConfiguration = AirForceEnv.getConfig(StaticConfiguration.class);
        routerManager.doMount(new StaticRouteMounter(staticConfiguration));
        //3、模板文件进行挂载
        routerManager.getRouter()
                .routeWithRegex(".+\\.html")
                .order(Integer.MAX_VALUE)
                .blockingHandler(new TemplateHandlerImpl(new HtmlTemplateEngine(),"template","text/html"));
        //4、挂载其他Mounter
        routerMounters.forEach(mounter -> routerManager.doMount(mounter));
        this.routerManager = routerManager;
    }

    private void deployRestVerticle(Set<Class<? extends AbstractRestVerticle>> restVerticleClazz,
                                   Function<Class<? extends AbstractRestVerticle>,AbstractRestVerticle> factory){
        isWeb = ! restVerticleClazz.isEmpty();
        if (!isWeb){
            return;
        }
        //mount router to eventbus
        restVerticleClazz.forEach(clazz -> AbstractRestVerticle.mountRouter(clazz,routerManager.getRouter(),vertx.eventBus()));
        //deploy real verticle
        restVerticleClazz.forEach(clazz -> {
            AbstractRestVerticle tpl = factory.apply(clazz);
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
        CountDownLatch latch = new CountDownLatch(1);
        vertx.deployVerticle(httpServerVerticle,as->{
            latch.countDown();
            if (as.succeeded()){
                logger.info("server listen at port: {}! cost {}ms",port,timeMeter.end());
            }else{
                logger.error(as.cause().getMessage());
                as.cause().printStackTrace();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init(){
        initRouterManager(this.routerMounters);
        deployRestVerticle(this.restVerticleClazz,this.factory);
        startHttpServer(vertx,this.routerManager.getRouter());
    }


    public List<RouterMounter> getRouterMounters() {
        return routerMounters;
    }

    public void setRouterMounters(List<RouterMounter> routerMounters) {
        this.routerMounters = routerMounters;
    }

    public Set<Class<? extends AbstractRestVerticle>> getRestVerticleClazz() {
        return restVerticleClazz;
    }

    public void setRestVerticleClazz(Set<Class<? extends AbstractRestVerticle>> restVerticleClazz) {
        this.restVerticleClazz = restVerticleClazz;
    }

    public Function<Class<? extends AbstractRestVerticle>, AbstractRestVerticle> getFactory() {
        return factory;
    }

    public void setFactory(Function<Class<? extends AbstractRestVerticle>, AbstractRestVerticle> factory) {
        this.factory = factory;
    }

    public RouterManager getRouterManager() {
        return routerManager;
    }

    public void setRouterManager(RouterManager routerManager) {
        this.routerManager = routerManager;
    }
}
