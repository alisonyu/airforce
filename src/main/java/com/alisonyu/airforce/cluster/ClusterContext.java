package com.alisonyu.airforce.cluster;

import com.alisonyu.airforce.cluster.config.ZookeeperConfig;
import com.alisonyu.airforce.common.tool.io.Network;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.core.config.SystemConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterContext {

    private Logger logger = LoggerFactory.getLogger(ClusterContext.class);
    private ClusterManager clusterManager;

    public void init(){
        ZookeeperConfig zookeeperConfig = AirForceEnv.getConfig(ZookeeperConfig.class);
        if (zookeeperConfig.isEnable()){
            JsonObject zkConfig = new JsonObject();
            zkConfig.put("zookeeperHosts",zookeeperConfig.getServers());
            zkConfig.put("rootPath",zookeeperConfig.getNamespace());
            zkConfig.put("sessionTimeout",zookeeperConfig.getSessionTimeout());
            zkConfig.put("connectTimeout",zookeeperConfig.getConnectTimeout());
            zkConfig.getJsonObject("retry",new JsonObject()).put("initialSleepTime",zookeeperConfig.getRetryIntervalSleepTime());
            zkConfig.getJsonObject("retry",new JsonObject()).put("maxTimes",zookeeperConfig.getRetryMaxTime());
            zkConfig.getJsonObject("retry",new JsonObject()).put("intervalTimes",zookeeperConfig.getRetryIntervalTime());
            ZookeeperClusterManager zookeeperClusterManager = new ZookeeperClusterManager(zkConfig);
            this.clusterManager = zookeeperClusterManager;
        }
    }


    public ClusterManager getClusterManager(){
        return this.clusterManager;
    }

    public String getHost(){
        SystemConfig systemConfig = AirForceEnv.getConfig(SystemConfig.class);
        String host;
        if (systemConfig.getRunMode().equals(SystemConfig.RUNMODE_DOCKER)){
            host = Network.getContainerIP();
        }else{
            host = "localhost";
        }
        logger.info("host:{}",host);
        return host;
    }


}
