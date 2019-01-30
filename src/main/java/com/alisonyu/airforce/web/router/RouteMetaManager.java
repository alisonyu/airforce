package com.alisonyu.airforce.web.router;

import com.alisonyu.airforce.common.constant.Strings;
import com.alisonyu.airforce.core.AirForceVerticle;

import javax.ws.rs.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class RouteMetaManager {

    private static Map<Class<? extends AirForceVerticle>,List<RouteMeta>> routeMetaMap = new ConcurrentHashMap<>();

    public static Map<Class<? extends AirForceVerticle>,List<RouteMeta>> getAllRuoteMetas(){
        return Collections.unmodifiableMap(routeMetaMap);
    }

    public static List<RouteMeta> getRouteMetas(Class<? extends AirForceVerticle> clazz){
        return routeMetaMap.computeIfAbsent(clazz, key -> {
            return getRouteMetasInternal(clazz);
        });
    }

    private static List<RouteMeta> getRouteMetasInternal(Class<? extends AirForceVerticle> clazz){
        String rootPath = clazz.isAnnotationPresent(Path.class) ? clazz.getAnnotation(Path.class).value() : Strings.SLASH;
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m->m.isAnnotationPresent(Path.class))
                .map(m-> new RouteMeta(rootPath,m))
                .collect(Collectors.toList());
    }


}
