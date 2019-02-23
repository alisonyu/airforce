package com.alisonyu.airforce.web.executor;

import com.alisonyu.airforce.common.tool.async.MethodAsyncExecutor;
import com.alisonyu.airforce.web.template.ModelView;
import com.alisonyu.airforce.web.template.TemplateEngineManager;
import io.reactivex.Flowable;
import io.vertx.core.Context;

import java.lang.reflect.Method;

/**
 * 代理方法执行器，处理特殊的返回格式
 */
public class MethodExecutor extends MethodAsyncExecutor {


    public MethodExecutor(Object instance, Method method, Context context) {
        super(instance, method, context);
    }

    @Override
    public Flowable<Object> invoke(Object[] args) {
        return super.invoke(args)
                .flatMap(MethodExecutor::modelViewDecorator);
    }

    public static Flowable<Object> modelViewDecorator(Object o){
        if (o instanceof ModelView){
            return  TemplateEngineManager.getInstance().render((ModelView)o).cast(Object.class);
        }else{
            return Flowable.just(o);
        }
    }

}
