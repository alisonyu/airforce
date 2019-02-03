package com.alisonyu.airforce.ratelimiter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterManager {

    private static Map<String,AirforceRateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    static void registerRateLimiter(AirforceRateLimiter airforceRateLimiter){
        rateLimiterMap.put(airforceRateLimiter.getName(),airforceRateLimiter);
    }



    public static Map<String,AirforceRateLimitConfig> getRateLimiterInfos(){
        Map<String,AirforceRateLimitConfig> infos = new HashMap<>();
        rateLimiterMap.forEach((key,value) -> {
            infos.put(key,value.getConfig());
        } );
        return Collections.unmodifiableMap(infos);
    }



}
