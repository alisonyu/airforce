package com.alisonyu.airforce.common.tool.instance;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @author yuzhiyi
 * @date 2018/9/13 23:24
 */
public class Anno {

	@SafeVarargs
	public static <T> T getAnnotationValue(AnnotatedElement element,
											String attr,
											T defaultValue,
											Class<? extends Annotation> anno1,
											Class<? extends Annotation>... annos){
		if (element.isAnnotationPresent(anno1)){
			return Instance.jvmInvoke(element.getAnnotation(anno1),attr);
		}
		for (Class<? extends Annotation> anno:annos){
			if (element.isAnnotationPresent(anno)){
				return Instance.jvmInvoke(element.getAnnotation(anno),attr);
			}
		}
		return defaultValue;
	}

	public static boolean isMark(AnnotatedElement element,Class<? extends Annotation> anno){
		return element.isAnnotationPresent(anno);
	}





}
