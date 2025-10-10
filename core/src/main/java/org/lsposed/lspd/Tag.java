package org.lsposed.lspd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import kotlin.annotation.AnnotationRetention;
import kotlin.annotation.Retention;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(AnnotationRetention.RUNTIME)
public @interface Tag {
    String value() default "";
}
