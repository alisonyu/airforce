package com.alisonyu.airforce.microservice;

import com.alisonyu.airforce.constant.*;
import com.alisonyu.airforce.microservice.core.param.ArgsBuilder;
import com.alisonyu.airforce.microservice.core.exception.ExceptionManager;
import com.alisonyu.airforce.microservice.meta.RouteMeta;
import com.alisonyu.airforce.microservice.router.DispatcherRouter;
import com.alisonyu.airforce.microservice.router.RouterMounter;
import com.alisonyu.airforce.microservice.router.UnsafeLocalMessageCodec;
import com.alisonyu.airforce.tool.instance.Instance;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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
public abstract class AbstractRestVerticle extends AbstractVerticle implements RouterMounter {

	private static Logger logger = LoggerFactory.getLogger(AbstractRestVerticle.class);

	private String rootPath;

	@Override
	public void start() throws Exception {
		super.start();
	}

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		mountEventBus();
	}

	/**
	 * 进行路由的挂载
	 */
	@Override
	public void mount(Router router){
		Class<? extends AbstractRestVerticle> clazz = this.getClass();
		this.rootPath = clazz.isAnnotationPresent(Path.class) ? clazz.getAnnotation(Path.class).value() : Strings.SLASH ;
		//将在RestVerticle定义的方法转化为RouteMeta
		List<RouteMeta> routeMetas = getRouteMetas(clazz,rootPath);
		//debug routeMeta
		getVertx().runOnContext((e)->routeMetas.forEach(routeMeta -> logger.info(routeMeta.toString())));
		//绑定路由
		routeMetas.forEach(routeMeta -> router.route(routeMeta.getHttpMethod(),routeMeta.getPath())
				.handler(ctx-> {
					//根据不同模式分发事件到不同线程中执行
					//todo 处理异常
					if (routeMeta.getMode() == CallMode.ASYNC){
						getVertx().runOnContext(e-> attack(routeMeta,ctx));
					}else{
						getVertx().executeBlocking(f->{
							attack(routeMeta,ctx);
							f.complete();
						},false,null);
					}
		}));
	}


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
	public void mountEventBus(){
		Class<? extends AbstractRestVerticle> clazz = this.getClass();
		this.rootPath = clazz.isAnnotationPresent(Path.class) ? clazz.getAnnotation(Path.class).value() : Strings.SLASH ;
		//将在RestVerticle定义的方法转化为RouteMeta
		List<RouteMeta> routeMetas = getRouteMetas(clazz,rootPath);
		EventBus eventBus = getVertx().eventBus();
		routeMetas.forEach(routeMeta -> {
			String url = DispatcherRouter.getDispathcerAddress(routeMeta.getHttpMethod(),routeMeta.getPath());
			eventBus.<RoutingContext>localConsumer(url)
					.handler(event -> {
						RoutingContext ctx = event.body();
						if (routeMeta.getMode() == CallMode.ASYNC){
							runOnContext(routeMeta,ctx);
						}else{
							runOnWorker(routeMeta,ctx);
						}
					});
		});
	}

	private static final DeliveryOptions DELIVERY_OPTIONS = new DeliveryOptions().setCodecName(UnsafeLocalMessageCodec.class.getName());
	private static void dispatchToEventBus(EventBus eventBus,RouteMeta routeMeta,RoutingContext context){
		String url = DispatcherRouter.getDispathcerAddress(routeMeta.getHttpMethod(),routeMeta.getPath());
		eventBus.send(url,context,DELIVERY_OPTIONS);
	}

	private void runOnContext(RouteMeta routeMeta,RoutingContext ctx){
		getVertx().runOnContext(e -> attack(routeMeta,ctx));
	}

	private void runOnWorker(RouteMeta routeMeta,RoutingContext ctx){
		getVertx().executeBlocking(f->{
			attack(routeMeta,ctx);
			f.complete();
		},false,null);
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

	private void attack(RouteMeta meta,RoutingContext context){
		try{
			//1、构造参数
			Object[] args = ArgsBuilder.build(meta,context);
			Object out;
			//2、调用对应方法
			try{
				out = methodInvoke(meta, AbstractRestVerticle.this,args);
			}catch (Exception e){
				out = ExceptionManager.handleException(meta,context,this,e);
			}
			//3、返回结果
			doResponse(meta,out,context);
		}
		//4、处理异常
		catch (Exception e){

		}
	}


	private Object methodInvoke(RouteMeta meta,Object target,Object[] args){
		Method method = meta.getProxyMethod();
		return Instance.enhanceInvoke(target,method.getName(),args);
	}

	private void doResponse(RouteMeta meta,Object result,RoutingContext context){
		HttpServerResponse resp = context.response();
		//1、设置Content-type
		String produceType = meta.getProduceType();
		resp.putHeader(Headers.CONTENT_TYPE,produceType);
		//2、异步的等待结果到来之后再响应
		if (result instanceof Future){
			Future<Object> future = (Future<Object>) result;
			future.setHandler(event -> {
				if (event.succeeded()){
					//3、序列化结果
					String out = serialize(event.result(),produceType);
					//4、写入
					resp.end(out);
				}else{
					ExceptionManager.handleException(meta,context, AbstractRestVerticle.this, (Exception) event.cause());
				}
			});
		}
		//2、同步的立刻响应
		else{
			//3、序列化结果
			String out = serialize(result,produceType);
			//4、写入
			resp.end(out);
		}

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
