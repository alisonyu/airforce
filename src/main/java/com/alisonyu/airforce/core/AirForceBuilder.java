package com.alisonyu.airforce.core;

import com.alisonyu.airforce.cluster.config.ZookeeperConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.core.banner.Banner;
import com.alisonyu.airforce.web.exception.ExceptionHandler;
import com.alisonyu.airforce.web.router.mounter.RouterMounter;
import com.alisonyu.airforce.web.template.TemplateEngineManager;
import com.alisonyu.airforce.web.transfer.UnsafeLocalMessageCodec;
import com.alisonyu.airforce.microservice.utils.ServiceMessageCodec;
import com.alisonyu.airforce.common.tool.async.AsyncHelper;
import com.alisonyu.airforce.common.tool.TimeMeter;
import com.alisonyu.airforce.common.tool.instance.Instance;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxInfluxDbOptions;
import io.vertx.reactivex.RxHelper;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    private Set<Class<? extends AirForceVerticle>> verticleClassSet = new HashSet<>();
    private Function<Class<? extends AirForceVerticle>, AirForceVerticle> verticleFactory;
    private List<ExceptionHandler> exceptionHandlers = Collections.emptyList();
    private List<RouterMounter> routerMounters = Collections.emptyList();
    private List<Object> services = Collections.emptyList();
    private ClusterManager clusterManager;
    private WebInitializer webInitializer;
    private ServiceInitializer serviceInitializer;
    private AtomicBoolean inited = new AtomicBoolean(false);

    //todo 先初始化资源，再进行操作
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
        TimeMeter timeMeter = new TimeMeter();
        timeMeter.start();
        Vertx vertx = this.vertx;
        if (vertx == null){
            VertxOptions vertxOptions = this.vertxOptions == null ? new VertxOptions() : this.vertxOptions;

            //init metric
//            vertxOptions.setMetricsOptions(
//                new DropwizardMetricsOptions().setJmxEnabled(true).setJmxDomain("airforce")
//                    );

//            vertxOptions.setMetricsOptions(
//                    new MicrometerMetricsOptions()
//                    .setInfluxDbOptions(new VertxInfluxDbOptions()
//                        .setEnabled(true)
//                        .setUri("http://localhost:8086")
//                        .setDb("airforce")
//                    ).setEnabled(true)
//            );

            // init cluster
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
        vertx.eventBus().registerCodec(new ServiceMessageCodec());
        //对AsyncHelper注册Scheduler
        AsyncHelper.registerScheduler(RxHelper.blockingScheduler(vertx,false));

        long costTime = timeMeter.end();
        logger.info("init vertx cost {}ms",costTime);
    }

    /**
     * deploy rest verticles by instance
     */
    public AirForceBuilder restVerticles(List<AirForceVerticle> restVerticles){
        Set<Class<? extends AirForceVerticle>> classes = restVerticles.stream()
                .map(AirForceVerticle::getClass)
                .distinct()
                .collect(Collectors.toSet());
        Function<Class<? extends AirForceVerticle>, AirForceVerticle> factory = clazz -> restVerticles.stream()
                .filter(v -> clazz.isAssignableFrom(v.getClass()))
                .findFirst()
                .orElse(null);
        airforceVerticles(classes,factory);
        return this;
    }

    /**
     * deploy restVerticles by class
     */
    public AirForceBuilder airforceVerticles(Set<Class<? extends AirForceVerticle>> set){
        airforceVerticles(set, Instance::instance);
        return this;
    }


    /**
     * deploy rest verticles by class and factory function
     */
    public AirForceBuilder airforceVerticles(Set<Class<? extends AirForceVerticle>> classSet,
                                         Function<Class<? extends AirForceVerticle>, AirForceVerticle> factory){

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

    public AirForceBuilder template(Function<Vertx, TemplateEngine> engineFunction,String suffix,boolean isDefault){
        TemplateEngineManager.getInstance().registerTemplate(engineFunction.apply(vertx),suffix,isDefault);
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
