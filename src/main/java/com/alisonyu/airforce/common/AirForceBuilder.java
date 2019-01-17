package com.alisonyu.airforce.common;

import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.constant.Banner;
import com.alisonyu.airforce.microservice.AbstractRestVerticle;
import com.alisonyu.airforce.microservice.ServiceInitializer;
import com.alisonyu.airforce.microservice.WebInitializer;
import com.alisonyu.airforce.microservice.core.exception.ExceptionHandler;
import com.alisonyu.airforce.microservice.router.RouterMounter;
import com.alisonyu.airforce.microservice.router.UnsafeLocalMessageCodec;
import com.alisonyu.airforce.tool.AsyncHelper;
import com.alisonyu.airforce.tool.instance.Instance;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.RxHelper;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用于指定启动Airforce Application的配置
 */
public class AirForceBuilder {

    private Vertx vertx;
    private VertxOptions vertxOptions;
    private List<AbstractRestVerticle> restVerticles = Collections.emptyList();
    private Set<Class<? extends AbstractRestVerticle>> verticleClassSet = Collections.emptySet();
    private Function<Class<? extends AbstractRestVerticle>,AbstractRestVerticle> verticleFactory;
    private List<ExceptionHandler> exceptionHandlers = Collections.emptyList();
    private List<RouterMounter> routerMounters = Collections.emptyList();
    private List<Object> services = Collections.emptyList();
    private ClusterManager clusterManager;
    private WebInitializer webInitializer;
    private ServiceInitializer serviceInitializer;

    public static AirForceBuilder build(){
        AirForceBuilder builder = new AirForceBuilder();
        return builder;
    }

    public AirForceBuilder vertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    public AirForceBuilder vertxOptions(VertxOptions vertxOptions){
        this.vertxOptions = vertxOptions;
        return this;
    }

    private void initVertx(){
        Vertx vertx = this.vertx;
        if (vertx == null){
            VertxOptions vertxOptions = this.vertxOptions == null ? new VertxOptions() : this.vertxOptions;
            if (this.clusterManager != null){
                vertxOptions.setClustered(true);
                vertxOptions.setClusterManager(clusterManager);
            }
            if (vertxOptions.isClustered()){
                vertx = AsyncHelper.<Vertx>blockingGet(handler-> Vertx.clusteredVertx(vertxOptions,handler));
            }else{
                vertx = AsyncHelper.<Vertx>blockingGet(handler -> Vertx.vertx(vertxOptions));
            }
            this.vertx = vertx;
        }
        //对EventBus注册本地Local Codec
        vertx.eventBus().registerCodec(new UnsafeLocalMessageCodec());
        //对AsyncHelper注册Scheduler
        AsyncHelper.registerScheduler(RxHelper.blockingScheduler(vertx,false));
    }

    /**
     * deploy rest verticles by instance
     */
    public AirForceBuilder restVerticles(List<AbstractRestVerticle> restVerticles){
        Set<Class<? extends AbstractRestVerticle>> classes = restVerticles.stream()
                .map(AbstractRestVerticle::getClass)
                .distinct()
                .collect(Collectors.toSet());
        Function<Class<? extends AbstractRestVerticle>,AbstractRestVerticle> factory = clazz -> restVerticles.stream()
                .filter(v -> clazz.isAssignableFrom(v.getClass()))
                .findFirst()
                .orElse(null);
        restVerticles(classes,factory);
        return this;
    }

    /**
     * deploy restVerticles by class
     */
    public AirForceBuilder restVerticle(Set<Class<? extends AbstractRestVerticle>> set){
        restVerticles(set, Instance::instance);
        return this;
    }


    /**
     * deploy rest verticles by class and factory function
     */
    public AirForceBuilder restVerticles(Set<Class<? extends AbstractRestVerticle>> classSet,
                                         Function<Class<? extends AbstractRestVerticle>,AbstractRestVerticle> factory){

        this.verticleClassSet = classSet;
        this.verticleFactory = factory;
        return this;
    }

    /**
     * register user defined router mounter
     */
    public AirForceBuilder routerMounters(List<RouterMounter> mounters){
        this.routerMounters = mounters;
        return this;
    }

    /**
     * rest exception handler
     */
    public AirForceBuilder restExceptionHandler(List<ExceptionHandler> exceptionHandlers){
        this.exceptionHandlers = exceptionHandlers;
        return this;
    }

    /**
     * publisher your services
     */
    public AirForceBuilder publish(List<Object> services){
        this.services = services;
        return this;
    }

    public AirForceBuilder setClusterManager(ClusterManager clusterManager){
        this.clusterManager = clusterManager;
        return this;
    }

    /**
     * run airforce application
     */
    public void run(Class<?> startClazz,String[] args){
        //show banner
        System.out.println(Banner.defaultBanner);
        //init vertx
        this.initVertx();
        this.webInitializer = new WebInitializer(this.vertx);
        this.serviceInitializer = new ServiceInitializer(this.vertx);
        //init config
        AirForceEnv.init(vertx,null);
        //deploy soa service
        serviceInitializer.publishServices(services);
        //deploy web
        webInitializer.setRestVerticleClazz(verticleClassSet);
        webInitializer.setFactory(verticleFactory);
        webInitializer.init();
    }





}
