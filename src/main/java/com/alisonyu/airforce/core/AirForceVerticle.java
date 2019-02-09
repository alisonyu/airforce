package com.alisonyu.airforce.core;

import com.alisonyu.airforce.common.constant.Strings;
import com.alisonyu.airforce.common.tool.SerializeUtils;
import com.alisonyu.airforce.web.executor.ArgsBuilder;
import com.alisonyu.airforce.web.exception.ExceptionManager;
import com.alisonyu.airforce.web.executor.RestExecuteProxy;
import com.alisonyu.airforce.web.router.RouteMeta;
import com.alisonyu.airforce.web.router.DispatcherRouter;
import com.alisonyu.airforce.microservice.provider.ServiceProvider;
import com.alisonyu.airforce.microservice.provider.ServicePublisher;
import com.alisonyu.airforce.common.tool.instance.Instance;
import com.alisonyu.airforce.web.constant.CallMode;
import com.alisonyu.airforce.web.constant.http.ContentTypes;
import com.alisonyu.airforce.web.constant.http.Headers;
import com.alisonyu.airforce.web.router.RouteMetaManager;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.RxHelper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 用于发布Rest以及SOA服务
 * @author yuzhiyi
 * @date 2018/9/12 10:13
 */
public class AirForceVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(AirForceVerticle.class);
    private Scheduler vertxScheduler;
    private Scheduler blockingScheduler;

	@Override
	public void start() throws Exception {
		super.start();
	}

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		//mount rest to eb
		//mountEventBus();
		DispatcherRouter.mountRouter(this.getClass(),vertx.eventBus());
		//mount service to eb
		RestExecuteProxy.doMount(this,vertx);

		publishService();
		vertxScheduler = RxHelper.scheduler(vertx);
		blockingScheduler = RxHelper.blockingScheduler(vertx,false);
	}

	/**
	 * 覆盖该方法可以自定义部署Option
	 */
	public DeploymentOptions getDeployOption(){
		return new DeploymentOptions();
	}



	private void publishService(){
		ServiceProvider serviceProvider = this.getClass().getAnnotation(ServiceProvider.class);
		if (serviceProvider == null){
			return;
		}
		// publish airforce verticle service
		ServicePublisher.publish(vertx,this);
	}

}
