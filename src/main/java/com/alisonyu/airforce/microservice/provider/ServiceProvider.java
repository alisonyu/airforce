package com.alisonyu.airforce.microservice.provider;
import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceProvider {

    String defaultVersion = "1.0.0";
    String defaultGroup = "airForce";
    Class<?> defaultClass = Object.class;

    Class<?> serviceClass() default Object.class;

    String group() default defaultGroup;

    String version() default defaultVersion;


}
