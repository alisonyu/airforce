package com.alisonyu.airforce.common.tool.async;

import com.alisonyu.airforce.common.tool.instance.Instance;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.reactivex.RxHelper;

import java.lang.reflect.Method;

/**
 * thread safe async reflect method call
 * the result will be represented to Flowable
 */
//todo 改成函数式接口 ; 处理void的情况
public class MethodAsyncExecutor {

    private Method method;
    private Object instance;
    private Context context;
    private Scheduler contextScheduler;

    @Deprecated
    public MethodAsyncExecutor(Object instance, Method method, Context context){
        this.instance = instance;
        this.method = method;
        if (context != null){
            this.context = context;
            this.contextScheduler = RxHelper.scheduler(context);
        }
    }

    public MethodAsyncExecutor(Object instance,Method method){
        this.instance = instance;
        this.method = method;
    }

    public Flowable<Object> invoke(Object[] args){
        return invoke0(args)
                .flatMap(o-> {

        if (o instanceof Flowable){
            return (Flowable<Object>) o;
        }
        else if (o instanceof Future){
            return AsyncHelper.fromFuture((Future<Object>) o);
        }
        else{
            return Flowable.<Object>just(o);
        }

        });
    }

    /**
     * 该executor不应该决定方法是在什么线程上执行的，这个由上层调用者决定
     * @param args
     * @return
     */
    private Flowable<Object> invoke0(Object[] args){
        return Single.fromCallable(()-> {
            return Instance.enhanceInvoke(instance,method.getName(),args);
        }).toFlowable();
    }

}
