package com.alisonyu.airforce.web.executor;

import com.alisonyu.airforce.common.tool.async.MethodAsyncExecutor;
import com.alisonyu.airforce.core.AirForceVerticle;
import com.alisonyu.airforce.web.constant.CallMode;
import com.alisonyu.airforce.web.constant.http.Headers;
import com.alisonyu.airforce.web.exception.ExceptionManager;
import com.alisonyu.airforce.web.router.DispatcherRouter;
import com.alisonyu.airforce.web.router.RouteMeta;
import com.alisonyu.airforce.web.router.RouteMetaManager;
import com.alisonyu.airforce.web.template.ModelView;
import com.alisonyu.airforce.web.template.TemplateEngineManager;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.RxHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RestExecuteProxy {

    private AirForceVerticle proxyInstance;
    private Vertx vertx;
    private Scheduler vertxScheduler;
    private Scheduler blockingScheduler;
    //todo Map<Method,Function<Object[],Flowable<Object>>> asyncExecutorMap
    private Map<Method, MethodAsyncExecutor> asyncExecutorMap = new ConcurrentHashMap<>();
    private List<MessageConsumer> messageConsumers = new ArrayList<>();

    public static RestExecuteProxy doMount(AirForceVerticle proxyVerticle,Vertx vertx){
        Class<? extends AirForceVerticle> clazz = proxyVerticle.getClass();
        RestExecuteProxy proxy = new RestExecuteProxy();
        proxy.proxyInstance = proxyVerticle;
        proxy.vertx = vertx;
        proxy.vertxScheduler = RxHelper.scheduler(vertx);
        proxy.blockingScheduler = RxHelper.blockingScheduler(vertx,false);
        proxy.mountRequestMethodToEventBus();
        return proxy;
    }

    private void mountRequestMethodToEventBus(){
        List<RouteMeta> routeMetas = RouteMetaManager.getRouteMetas(proxyInstance.getClass());
        EventBus eb = vertx.eventBus();
        routeMetas.forEach(routeMeta -> registerMethodToEventBus(routeMeta,eb) );
    }

    private MessageConsumer<RoutingContext> registerMethodToEventBus(RouteMeta routeMeta, EventBus eb){
        String dispatcherAddress = DispatcherRouter.getDispatcherAddress(routeMeta.getHttpMethod(),routeMeta.getPath());
        //todo 改为rxjava的形式
        return eb.<RoutingContext>localConsumer(dispatcherAddress,event -> {
            RoutingContext routingContext = event.body();
            //todo 类为资源做限速，decorator该方法
            execute(routingContext,routeMeta);
        });
    }

    private void execute(RoutingContext ctx,RouteMeta routeMeta){

        Scheduler scheduler = routeMeta.getMode() == CallMode.EventLoop ? vertxScheduler : blockingScheduler;
        MethodAsyncExecutor methodAsyncExecutor = getAsyncExecutor(routeMeta.getProxyMethod());

        final HttpServerResponse response = ctx.response();
        response.putHeader(Headers.CONTENT_TYPE,routeMeta.getProduceType());
        response.setChunked(true);
        //todo 方法级别限速入口，使用operator进行限速
        //todo 考虑返回类型为Null的情况
        //解析参数，并将参数作为入口
        Flowable.fromCallable(()-> ArgsBuilder.build(routeMeta,ctx))
                //方法调用
                .flatMap(args -> methodAsyncExecutor.invoke(args))
                .onErrorReturn(t -> ExceptionManager.handleException(routeMeta,ctx,this,t))
                //将结果根据ProduceType序列化
                .map(in -> Serializer.serializer(in,routeMeta.getProduceType()))
                //以上结果根据异步或者同步在不同的模式下选择不同的调度器
                .subscribeOn(scheduler)
                //写结束后关闭流
                .doFinally(()-> {
                    if (!response.ended()){
                        response.end();
                    }
                })
                //将结果写入到http response中
                .subscribe(buffer->{
                   response.write(buffer);
                });
    }

    private MethodAsyncExecutor getAsyncExecutor(Method method){
       return asyncExecutorMap.computeIfAbsent(method,key-> {
           return new MethodExecutor(proxyInstance,method,vertx.getOrCreateContext());
       });
    }

    //todo unregister
    public void unRegister(){

    }

}
