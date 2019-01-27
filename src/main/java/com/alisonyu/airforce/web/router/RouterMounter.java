package com.alisonyu.airforce.web.router;

import io.vertx.ext.web.Router;

/**
 * 用于注册路由
 * @author yuzhiyi
 * @date 2018/10/6 21:02
 */
public interface RouterMounter {

	void mount(Router router);

}
