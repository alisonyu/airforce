package com.alisonyu.airforce.microservice.service.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MethodNameUtils {

    public static String getName(String itfName,String methodName,String[] argsName,String group,String version){
        return itfName+"#"+methodName+"#("+ String.join(",", argsName)+")#"+group+"#"+version;
    }

    public static String getName(Class itf, Method method,String group,String version){
        String itfName = itf.getName();
        String methodName = method.getName();
        String[] argsName = Arrays.stream(method.getParameterTypes())
                                .map(Class::getName)
                                .collect(Collectors.toList())
                                .toArray(new String[]{});
        return getName(itfName,methodName,argsName,group,version);
    }

}
