package com.alisonyu.airforce.web.router.mounter;

import io.vertx.ext.web.Router;

/**
 * 用于对路由做自定义操作
 * @author yuzhiyi
 * @date 2018/10/6 21:02
 */
public interface RouterMounter {

	void mount(Router router);


}
