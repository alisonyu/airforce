package com.alisonyu.airforce.core;

import com.alisonyu.airforce.cluster.ClusterContext;
import com.alisonyu.airforce.common.tool.async.AsyncHelper;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.monitor.MonitorContext;
import com.alisonyu.airforce.web.AirForceWebContext;
import com.alisonyu.airforce.web.exception.ExceptionHandler;
import com.alisonyu.airforce.web.router.mounter.RouterMounter;
import com.alisonyu.airforce.web.template.TemplateRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.RxHelper;
import org.thymeleaf.context.WebContext;

import java.util.List;

public class AirForceContextBuilder {

    private Vertx vertx;
    private VertxOptions vertxOptions;
    private Router router;
    private List<RouterMounter> routerMounterList;
    private List<ExceptionHandler> exceptionHandlers;
    private List<TemplateRegistry> templateRegistries;
    private List<Object> services;
    private boolean embedHttpServer = false;


    public static AirForceContextBuilder create(){
        return new AirForceContextBuilder();
    }

    public AirForceContextBuilder vertx(Vertx vertx){
        this.vertx = vertx;
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

    public AirForceContextBuilder emberHttpServer(boolean enable){
        this.embedHttpServer = enable;
        return this;
    }

    public AirForceContextBuilder publishServices(List<Object> services){
        this.services = services;
        return this;
    }

    public AirForceContext init(){

        AirForceEnv.init(this.getClass(),null);

        AirForceContext airForceContext = new AirForceContext();

        MonitorContext monitorContext = new MonitorContext(airForceContext);
        monitorContext.init();

        ClusterContext clusterContext = new ClusterContext();
        clusterContext.init();

        VertxContext vertxContext;
        if (vertx != null){
            vertxContext = new VertxContext(vertx);
        }else if (vertxOptions != null){
            vertxContext = new VertxContext(vertxOptions);
        }else{
            vertxContext = new VertxContext();
        }
        vertxContext.init();
        vertx  = vertxContext.getVertx();

        EventbusContext eventbusContext = new EventbusContext(vertxContext.getVertx());
        eventbusContext.init();

        AsyncHelper.registerScheduler(RxHelper.blockingScheduler(vertxContext.getVertx(),false));

        //init microservice
        ServiceInitializer serviceInitializer = new ServiceInitializer(vertx);
        if (services != null){
            serviceInitializer.publishServices(services);
        }

        //init web
        AirForceWebContext webContext = new AirForceWebContext(vertx,airForceContext,router,routerMounterList,templateRegistries,exceptionHandlers,embedHttpServer);

        airForceContext.start(vertxContext.getVertx());

        return airForceContext;
    }



}
