package com.alisonyu.airforce.monitor;

import com.alisonyu.airforce.core.AirForceVerticle;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("metrics")
public class MonitorRestVerticle extends AirForceVerticle {

    private MetricsService metricsService;

    @Override
    public void start() throws Exception {
        super.start();
        this.metricsService = MetricsService.create(vertx);
    }

    @GET
    @Path("/vertx")
    public Flowable<JsonObject> vertxMetrics(){
        return Flowable.just(
         metricsService.getMetricsSnapshot(vertx));
    }

}
