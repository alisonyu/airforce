package com.alisonyu.airforce.ratelimiter;

public class RequestTooFastException extends RuntimeException {

    public RequestTooFastException(){
        super();
    }

    public RequestTooFastException(String message){
        super(message);
    }

}
