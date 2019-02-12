package com.alisonyu.airforce.core.config;

import com.alisonyu.airforce.cluster.ClusterContext;
import com.alisonyu.airforce.cluster.config.ZookeeperConfig;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.monitor.MonitorContext;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

public class VertxConfig {

    private VertxOptions vertxOptions;
    private ClusterContext clusterContext;
    private MonitorContext monitorContext;

    public VertxConfig(VertxOptions vertxOptions, ClusterContext clusterContext, MonitorContext monitorContext){
        this.vertxOptions = vertxOptions;
        this.monitorContext = monitorContext;
        this.clusterContext = clusterContext;
        init();
    }

    private void init(){
        initClusterManager();
        initMetric();
    }


    private void initClusterManager(){
        //if user not define cluster
        if (!vertxOptions.isClustered() && vertxOptions.getClusterManager() == null){
            if (clusterContext.getClusterManager() != null){
                vertxOptions.setClusterHost(clusterContext.getHost());
                vertxOptions.setClusterManager(clusterContext.getClusterManager());
            }
        }
    }

    private void initMetric(){
        //if user not define metric,use dropWizard
        if (!vertxOptions.getMetricsOptions().isEnabled()){
            if (monitorContext.getMetricOptions() != null){
                vertxOptions.setMetricsOptions(monitorContext.getMetricOptions());
            }
        }
    }

    public VertxOptions getVertxOptions(){
        return this.vertxOptions;
    }


}
