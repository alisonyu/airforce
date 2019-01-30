package com.alisonyu.airforce.web.executor.param;

import com.alisonyu.airforce.common.tool.functional.Case;
import com.alisonyu.airforce.common.tool.functional.Functions;
import com.alisonyu.airforce.common.tool.instance.Anno;
import com.alisonyu.airforce.web.anno.BodyParam;
import com.alisonyu.airforce.web.anno.SessionParam;
import com.alisonyu.airforce.web.constant.ParamType;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParamMetaFactory {

    public static ParamMeta[] getParamMeta(Method method){
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnos = method.getParameterAnnotations();
        Parameter[] parameters = method.getParameters();
        ParamMeta[] paramMetas = new ParamMeta[paramTypes.length];
        for (int idx = 0;idx < paramTypes.length; idx++){

            Class<?> type = paramTypes[idx];
            Annotation[] annos = paramAnnos[idx];
            List<Class<? extends Annotation>> annoTypes = Arrays.stream(annos).map(Annotation::annotationType).collect(Collectors.toList());
            Parameter parameter = parameters[idx];
            String name;
            String defaultValue;

            //确定ParamType
            ParamType paramType = Functions.matchAny(annoTypes,ParamType.QUERY_PARAM,
                    Case.of(QueryParam.class,(t)->  ParamType.QUERY_PARAM),
                    Case.of(FormParam.class,(t)-> ParamType.FORM_PARAM),
                    Case.of(CookieParam.class,(t)->ParamType.COOKIE_PARAM),
                    Case.of(HeaderParam.class,(t)->ParamType.HEADER_PARAM),
                    Case.of(PathParam.class,(t)->ParamType.PATH_PARAM),
                    Case.of(BodyParam.class,(t)-> ParamType.BODY_PARAM),
                    Case.of(SessionParam.class,(t)-> ParamType.SESSION_PARAM));

            //确定参数名和defaultValue
            name = Anno.getAnnotationValue(parameter,"value",null,
                    QueryParam.class,
                    FormParam.class,
                    CookieParam.class,
                    HeaderParam.class,
                    PathParam.class,
                    SessionParam.class);
            defaultValue = Anno.getAnnotationValue(parameter,"value",null,DefaultValue.class);
            boolean required = Anno.isMark(parameter,DefaultValue.class);
            ParamMeta meta = new ParamMeta(name,type,defaultValue,required);
            meta.setParamType(paramType);
            paramMetas[idx] = meta;
        }
        return paramMetas;
    }


}
