package com.alisonyu.airforce.tool;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.vertx.core.Future;

import java.util.concurrent.Callable;

/**
 * @author 四昭
 * @date 2018/11/17下午4:26
 */
public class AsyncHelper {

    private static Scheduler blockingScheduler;

    public static <T> Flowable<T> lift(Callable<T> callable){
        return Flowable.fromCallable(callable)
                .subscribeOn(blockingScheduler);
    }

    public static <T>Future<T> toFuture(Callable<T> callable){
        Future<T> future = Future.future();
        lift(callable)
                .doOnError(future::fail)
                .subscribe(future::complete);
        return future;
    }

    public static void registerScheduler(Scheduler scheduler){
        blockingScheduler = scheduler;
    }

    public static Scheduler getBlockingScheduler(){
        return blockingScheduler;
    }

}
