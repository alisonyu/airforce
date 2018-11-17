package com.alisonyu.airforce.microservice;

import com.alisonyu.airforce.constant.*;
import com.alisonyu.airforce.microservice.core.exception.ExceptionHandler;
import com.alisonyu.airforce.microservice.core.param.ArgsBuilder;
import com.alisonyu.airforce.microservice.core.exception.ExceptionManager;
import com.alisonyu.airforce.microservice.meta.RouteMeta;
import com.alisonyu.airforce.microservice.router.DispatcherRouter;
import com.alisonyu.airforce.microservice.router.RouterMounter;
import com.alisonyu.airforce.microservice.router.UnsafeLocalMessageCodec;
import com.alisonyu.airforce.tool.instance.Instance;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.ObservableHelper;
import io.vertx.reactivex.RxHelper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AbstractRestVerticle用于进行Rest接口编写
 * @author yuzhiyi
 * @date 2018/9/12 10:13
 */
public abstract class AbstractRestVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(AbstractRestVerticle.class);
    private Scheduler vertxScheduler;
    private Scheduler blockingScheduler;

	@Override
	public void start() throws Exception {
		super.start();
	}

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		mountEventBus();
		vertxScheduler = RxHelper.scheduler(vertx);
		blockingScheduler = RxHelper.blockingScheduler(vertx,false);
	}

	/**
	 * 注册路由
	 * @param clazz
	 * @param router
	 * @param eventBus
	 */
	public static void mountRouter(Class<? extends AbstractRestVerticle> clazz,Router router,EventBus eventBus){
		String rootPath = clazz.isAnnotationPresent(Path.class) ? clazz.getAnnotation(Path.class).value() : Strings.SLASH ;
		//将在RestVerticle定义的方法转化为RouteMeta
		List<RouteMeta> routeMetas = getRouteMetas(clazz,rootPath);
		//debug routeMeta
		routeMetas.forEach(routeMeta ->  logger.info(routeMeta.toString()));
		//绑定路由,派发到EventBus中执行
		routeMetas.forEach(routeMeta -> router.route(routeMeta.getHttpMethod(),routeMeta.getPath())
				.handler(ctx-> dispatchToEventBus(eventBus,routeMeta,ctx)));
	}


	/**
	 * 使用EventBus作为逻辑的分发
	 */
	private void mountEventBus(){
		final Class<? extends AbstractRestVerticle> clazz = this.getClass();
		final String rootPath = clazz.isAnnotationPresent(Path.class) ? clazz.getAnnotation(Path.class).value() : Strings.SLASH;
		//将在RestVerticle定义的方法转化为RouteMeta
		List<RouteMeta> routeMetas = getRouteMetas(clazz, rootPath);
		EventBus eventBus = getVertx().eventBus();
		routeMetas.forEach(routeMeta -> {
			String url = DispatcherRouter.getDispathcerAddress(routeMeta.getHttpMethod(),routeMeta.getPath());
			eventBus.<RoutingContext>localConsumer(url)
					.handler(event -> {
						RoutingContext ctx = event.body();
                        rxRun(ctx,routeMeta);
					});
		});
	}

	private static final DeliveryOptions DELIVERY_OPTIONS = new DeliveryOptions().setCodecName(UnsafeLocalMessageCodec.class.getName());
	private static void dispatchToEventBus(EventBus eventBus,RouteMeta routeMeta,RoutingContext context){
		String url = DispatcherRouter.getDispathcerAddress(routeMeta.getHttpMethod(),routeMeta.getPath());
		eventBus.send(url,context,DELIVERY_OPTIONS);
	}


	private void rxRun(RoutingContext ctx,RouteMeta routeMeta){
        Scheduler scheduler = routeMeta.getMode() == CallMode.ASYNC ? vertxScheduler : blockingScheduler;
	    //解析参数，并将参数作为入口
        Flowable.just(ArgsBuilder.build(routeMeta,ctx))
                //方法调用
                .map(args -> methodInvoke(routeMeta,AbstractRestVerticle.this,args))
                //如果异常，进入异常处理器，返回异常结果
                .onErrorReturn(e -> ExceptionManager.handleException(routeMeta,ctx,this,(Exception) e))
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



	private static List<RouteMeta> getRouteMetas(Class<? extends AbstractRestVerticle> clazz,String rootPath){
		return Arrays.stream(clazz.getDeclaredMethods())
				.filter(m->m.isAnnotationPresent(Path.class))
				.map(m-> new RouteMeta(rootPath,m))
				.collect(Collectors.toList());
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


	//todo 处理转换JSON异常
	private String serialize(Object in,String contentType){
		String out;
		switch (contentType){
			case ContentTypes.JSON:
				if (in instanceof JsonObject || in instanceof JsonArray){
					out = in.toString();
				}
				else if (List.class.isAssignableFrom(in.getClass())){
					out = new JsonArray((List) in).toString();
				}
				else if (in.getClass().isArray()){
					Object[] arr = (Object[]) in;
					out = new JsonArray(Arrays.asList(arr)).toString();
				}
				else{
					out = JsonObject.mapFrom(in).toString();
				}
				break;
			default: out = in.toString(); break;
		}
		return out;
	}




}
