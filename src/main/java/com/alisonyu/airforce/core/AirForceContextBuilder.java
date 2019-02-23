package com.alisonyu.airforce.core;

import com.alisonyu.airforce.cluster.ClusterContext;
import com.alisonyu.airforce.common.tool.async.AsyncHelper;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.core.config.VertxConfig;
import com.alisonyu.airforce.monitor.MonitorContext;
import com.alisonyu.airforce.web.AirForceWebContext;
import com.alisonyu.airforce.web.exception.ExceptionHandler;
import com.alisonyu.airforce.web.router.mounter.RouterMounter;
import com.alisonyu.airforce.web.template.TemplateRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.reactivex.RxHelper;

import java.util.List;
import java.util.function.Function;

public class AirForceContextBuilder {

    private Vertx vertx;
    private VertxOptions vertxOptions;
    private Router router;
    private List<RouterMounter> routerMounterList;
    private List<ExceptionHandler> exceptionHandlers;
    private List<TemplateRegistry> templateRegistries;
    private boolean embedHttpServer = false;
    private Function<Vertx, SessionStore> sessionStoreFacotry;
    private HttpServerOptions httpServerOptions;
    private String[] args;


    public static AirForceContextBuilder create(){
        return new AirForceContextBuilder();
    }

    public AirForceContextBuilder vertx(Vertx vertx){
        this.vertx = vertx;
        return this;
    }

    public AirForceContextBuilder args(String[] args){
        this.args = args;
        return this;
    }

    public AirForceContextBuilder vertxOption(VertxOptions vertxOptions){
        this.vertxOptions = vertxOptions;
        return this;
    }

    public AirForceContextBuilder router(Router router){
        this.router  = router;
        return this;
    }

    public AirForceContextBuilder exceptionHandler(List<ExceptionHandler> exceptionHandlers){
        this.exceptionHandlers = exceptionHandlers;
        return this;
    }

    public AirForceContextBuilder routerMounters(List<RouterMounter> routerMounters){
        this.routerMounterList = routerMounters;
        return this;
    }

    public AirForceContextBuilder templateEngines(List<TemplateRegistry> templateRegistries){
        this.templateRegistries = templateRegistries;
        return this;
    }

    public AirForceContextBuilder session(Function<Vertx, SessionStore> sessionStoreFacotry){
        this.sessionStoreFacotry = sessionStoreFacotry;
        return this;
    }

    public AirForceContextBuilder httpServerOption(HttpServerOptions httpServerOptions){
        this.httpServerOptions = httpServerOptions;
        return this;
    }

    public AirForceContextBuilder emberHttpServer(boolean enable){
        this.embedHttpServer = enable;
        return this;
    }


    public AirForceContext init(){

        AirForceEnv.init(this.getClass(),args);

        LogContext.init(args);

        AirForceContext airForceContext = new AirForceContext();

        MonitorContext monitorContext = new MonitorContext(airForceContext);
        monitorContext.init();

        ClusterContext clusterContext = new ClusterContext();
        clusterContext.init();

        VertxContext vertxContext;
        if (vertx != null){
            vertxContext = new VertxContext(vertx);
        }else if (vertxOptions != null){
            VertxConfig vertxConfig = new VertxConfig(vertxOptions,clusterContext,monitorContext);
            vertxContext = new VertxContext(vertxOptions);
        }else{
            VertxConfig vertxConfig = new VertxConfig(new VertxOptions(),clusterContext,monitorContext);
            vertxContext = new VertxContext(vertxConfig.getVertxOptions());
        }
        vertxContext.init();
        vertx  = vertxContext.getVertx();

        EventbusContext eventbusContext = new EventbusContext(vertxContext.getVertx());
        eventbusContext.init();

        AsyncHelper.registerScheduler(RxHelper.blockingScheduler(vertxContext.getVertx(),false));



        //init web
        SessionStore sessionStore = null;
        if (sessionStoreFacotry != null){
            sessionStore = sessionStoreFacotry.apply(vertx);
        }
        AirForceWebContext webContext = new AirForceWebContext(vertx,airForceContext,router,routerMounterList,templateRegistries,exceptionHandlers,embedHttpServer,sessionStore,httpServerOptions);

        airForceContext.start(vertxContext.getVertx());

        return airForceContext;
    }



}
