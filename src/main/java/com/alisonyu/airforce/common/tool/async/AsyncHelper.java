package com.alisonyu.airforce.common.tool.async;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

    public static <T> Flowable<T> fromFuture(Future<T> future){
        return Flowable.fromPublisher(publisher -> {
            future.setHandler(as -> {
                if (as.succeeded()){
                    publisher.onNext(as.result());
                    publisher.onComplete();
                }else{
                    publisher.onError(as.cause());
                }
            });
        });
    }

    public static <T> Flowable<T> fromAsyncResult(Consumer<Handler<AsyncResult<T>>> consumer){
        return Flowable.fromPublisher(publisher -> {
           consumer.accept(as -> {
               if (as.succeeded()){
                   publisher.onNext(as.result());
                   publisher.onComplete();
               }else{
                   publisher.onError(as.cause());
               }
           });
        });
    }

    public static <T> T blockingGet(Consumer<Handler<AsyncResult<T>>> consumer){
        CompletableFuture<T> future = new CompletableFuture<>();
        consumer.accept(handler -> {
            if (handler.succeeded()){
                future.complete(handler.result());
            }else{
                future.completeExceptionally(handler.cause());
            }
        });
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public static void registerScheduler(Scheduler scheduler){
        blockingScheduler = scheduler;
    }

    public static Scheduler getBlockingScheduler(){
        return blockingScheduler;
    }

}
