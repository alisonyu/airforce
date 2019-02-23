package com.alisonyu.airforce.web.router.mounter;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;

public class SessionMounter implements RouterMounter {

    private SessionHandler sessionHandler;

    public SessionMounter(SessionStore sessionStore){
        this.sessionHandler  = SessionHandler.create(sessionStore);
    }


    @Override
    public void mount(Router router) {
        router.route().handler(sessionHandler);
    }

}
