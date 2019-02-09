package com.alisonyu.airforce.monitor;

import com.alisonyu.airforce.core.AirForceContext;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

public class MonitorContext {

    private AirForceContext context;

    public MonitorContext(AirForceContext context){
        this.context = context;
    }

    public void init(){
        context.queueVerticleInternal(MonitorRestVerticle::new);
    }

    public MetricsOptions getMetricOptions(){
        return new DropwizardMetricsOptions().setEnabled(true);
    }

}
