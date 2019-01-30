package com.alisonyu.airforce.web.executor;

import com.alisonyu.airforce.web.executor.param.ParamAcceptor;
import com.alisonyu.airforce.web.executor.param.ParamMeta;
import com.alisonyu.airforce.web.router.RouteMeta;
import io.vertx.ext.web.RoutingContext;

/**
 * 接受路由元信息和当前请求上下文，并返回元信息绑定的方法对应的参数列表
 * @author yuzhiyi
 * @date 2018/9/15 9:32
 */
public class ArgsBuilder {

	public static Object[] build(RouteMeta meta, RoutingContext context){
		final ParamMeta[] paramMetas = meta.getParamMetas();
		Object[] args = new Object[paramMetas.length];
		for (int idx = 0;idx<paramMetas.length;idx++){
			final ParamMeta paramMeta = paramMetas[idx];
			args[idx] = ParamAcceptor.accept(paramMeta,context);
		}
		return args;
	}


}
