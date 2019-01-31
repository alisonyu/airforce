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
     * 每一个AbstractRestVerticle实例都将会将相关的方法注册到EventBus上，使用EventBus来做负载均衡
	 * 使用EventBus作为具体方法的分发
	 */
	private void mountEventBus(){
		final Class<? extends AirForceVerticle> clazz = this.getClass();
		final String rootPath = clazz.isAnnotationPresent(Path.class) ? clazz.getAnnotation(Path.class).value() : Strings.SLASH;
		//将在RestVerticle定义的方法转化为RouteMeta
		List<RouteMeta> routeMetas = RouteMetaManager.getRouteMetas(clazz);
		EventBus eventBus = getVertx().eventBus();
		routeMetas.forEach(routeMeta -> {
			String url = DispatcherRouter.getDispatcherAddress(routeMeta.getHttpMethod(),routeMeta.getPath());
			eventBus.<RoutingContext>localConsumer(url)
					.handler(event -> {
						RoutingContext ctx = event.body();
						rxRun(ctx,routeMeta);
					});
		});
	}



	private void rxRun(RoutingContext ctx,RouteMeta routeMeta){
        Scheduler scheduler = routeMeta.getMode() == CallMode.EventLoop ? vertxScheduler : blockingScheduler;
	    //解析参数，并将参数作为入口
        Flowable.just(ArgsBuilder.build(routeMeta,ctx))
                //方法调用
                .map(args -> methodInvoke(routeMeta, AirForceVerticle.this,args))
                //判断结果类型，生成结果流
                .flatMap(res -> {
                    Publisher<Object> publisher = null;
                    if (res instanceof Publisher){
                        publisher = (Publisher<Object>) res;
                    }
                    else if (res instanceof Future){
                        publisher = Flowable.fromPublisher(source->{
                            Future<Object> future = (Future<Object>) res;
                            future.setHandler(asyncResult -> {
                                Object realResult = null;
                                if (asyncResult.succeeded()){
                                    realResult = asyncResult.result();
                                }else{
                                    realResult = ExceptionManager.handleException(routeMeta,ctx,this,(Exception)asyncResult.cause());
                                }
                                source.onNext(realResult);
                                source.onComplete();
                            });
                        });
                    }else{
                        publisher = Flowable.just(res);
                    }
                    return publisher;
                })
				.onErrorReturn(t -> ExceptionManager.handleException(routeMeta,ctx,this,t))
				//将结果根据ProduceType序列化
                .map(in -> serialize(in,routeMeta.getProduceType()))
                //以上结果根据异步或者同步在不同的模式下选择不同的调度器
                .subscribeOn(scheduler)
                //获得结果之后我们切换Worker线程
                .observeOn(blockingScheduler)
                //将结果写入到http response中
				.subscribe(content->{
                    HttpServerResponse resp = ctx.response();
                    //1、设置Content-type
                    String produceType = routeMeta.getProduceType();
                    resp.putHeader(Headers.CONTENT_TYPE,produceType);
                    //2、作出响应
                    resp.end(content);
                });
    }


	/**
	 * 覆盖该方法可以自定义部署Option
	 */
	public DeploymentOptions getDeployOption(){
		return new DeploymentOptions();
	}


	private Object methodInvoke(RouteMeta meta,Object target,Object[] args){
		Method method = meta.getProxyMethod();
		return Instance.enhanceInvoke(target,method.getName(),args);
	}


	private static String serialize(Object in,String contentType){
		String out;
		switch (contentType){
			case ContentTypes.JSON:
				out = SerializeUtils.toJsonString(in);
				break;
			default: out = in.toString(); break;
		}
		return out;
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
