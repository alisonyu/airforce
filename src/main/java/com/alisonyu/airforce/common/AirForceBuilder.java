package com.alisonyu.airforce.common;

import com.alisonyu.airforce.cloud.config.ZookeeperConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.constant.Banner;
import com.alisonyu.airforce.microservice.AbstractRestVerticle;
import com.alisonyu.airforce.microservice.DeamoVerticle;
import com.alisonyu.airforce.microservice.ServiceInitializer;
import com.alisonyu.airforce.microservice.WebInitializer;
import com.alisonyu.airforce.microservice.core.exception.ExceptionHandler;
import com.alisonyu.airforce.microservice.router.RouterMounter;
import com.alisonyu.airforce.microservice.router.UnsafeLocalMessageCodec;
import com.alisonyu.airforce.tool.AsyncHelper;
import com.alisonyu.airforce.tool.TimeMeter;
import com.alisonyu.airforce.tool.instance.Instance;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.RxHelper;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用于指定启动Airforce Application的配置
 */
public class AirForceBuilder {

    private static Logger logger = LoggerFactory.getLogger(AirForceBuilder.class);
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
    private AtomicBoolean inited = new AtomicBoolean(false);

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
                if (clusterManager instanceof ZookeeperClusterManager){
                    initZookeeperClusterManager(clusterManager);
                }
            }
            if (vertxOptions.isClustered()){
                vertx = AsyncHelper.<Vertx>blockingGet(handler-> Vertx.clusteredVertx(vertxOptions,handler));
            }else{
                vertx = Vertx.vertx(vertxOptions);
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

    /**
     * set user define cluster Manager
     */
    public AirForceBuilder setClusterManager(ClusterManager clusterManager){
        this.clusterManager = clusterManager;
        return this;
    }


    public AirForceBuilder cluster(){
        this.clusterManager = new ZookeeperClusterManager();
        return this;
    }

    public Vertx getVertx(){
        return this.vertx;
    }

    /**
     *  init Vertx
     */
    public synchronized Vertx init(){
        if (this.inited.get()){
            return this.vertx;
        }else{
            this.initVertx();
            this.inited.set(true);
            return this.vertx;
        }
    }

    /**
     * run airforce application
     */
    public void run(Class<?> startClazz,String[] args){
        TimeMeter timeMeter = new TimeMeter();
        timeMeter.start();
        //show banner
        showBanner();
        //init config
        AirForceEnv.init(startClazz,null);
        //init vertx
        this.init();
        //init web and soa service
        this.webInitializer = new WebInitializer(this.vertx);
        this.serviceInitializer = new ServiceInitializer(this.vertx);
        //deploy soa service
        serviceInitializer.publishServices(services);
        //deploy web
        webInitializer.setRestVerticleClazz(verticleClassSet);
        webInitializer.setFactory(verticleFactory);
        webInitializer.init();
        this.vertx.deployVerticle(new DeamoVerticle());
        long costTime = timeMeter.end();
        logger.info("Airforce Application started! cost {}ms",costTime);
    }

    private void showBanner(){
        System.out.println(Banner.defaultBanner);
    }

    private void initZookeeperClusterManager(ClusterManager clusterManager){
        if (clusterManager instanceof ZookeeperClusterManager){
            ZookeeperClusterManager zookeeperClusterManager = (ZookeeperClusterManager) clusterManager;
            if (zookeeperClusterManager.getConfig().isEmpty()){
                ZookeeperConfig zookeeperConfig = AirForceEnv.getConfig(ZookeeperConfig.class);
                JsonObject config = zookeeperClusterManager.getConfig();
                config.put("zookeeperHosts",zookeeperConfig.getServers());
                config.put("rootPath",zookeeperConfig.getNamespace());
                config.put("sessionTimeout",zookeeperConfig.getSessionTimeout());
                config.put("connectTimeout",zookeeperConfig.getConnectTimeout());
                config.getJsonObject("retry",new JsonObject()).put("initialSleepTime",zookeeperConfig.getRetryIntervalSleepTime());
                config.getJsonObject("retry",new JsonObject()).put("maxTimes",zookeeperConfig.getRetryMaxTime());
                config.getJsonObject("retry",new JsonObject()).put("intervalTimes",zookeeperConfig.getRetryIntervalTime());
            }
        }
    }





}
