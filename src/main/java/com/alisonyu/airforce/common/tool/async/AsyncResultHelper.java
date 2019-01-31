package com.alisonyu.airforce.common.tool.async;

import io.vertx.core.AsyncResult;

public class AsyncResultHelper {

    public static  <T>AsyncResult<T> success(T t){
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return t;
            }

            @Override
            public Throwable cause() {
                return null;
            }

            @Override
            public boolean succeeded() {
                return true;
            }

            @Override
            public boolean failed() {
                return false;
            }
        };
    }


    public static  <T>AsyncResult<T> fail(Throwable t){
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return null;
            }

            @Override
            public Throwable cause() {
                return t;
            }

            @Override
            public boolean succeeded() {
                return false;
            }

            @Override
            public boolean failed() {
                return true;
            }
        };
    }



}
