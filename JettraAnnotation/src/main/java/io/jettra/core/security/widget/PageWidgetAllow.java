package io.jettra.core.security.widget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jcf.AppRole;

/**
 * Define los privilegios mínimos necesarios para renderizar o ingresar a una vista o página completa.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PageWidgetAllow {
    AppRole[] role() default {};
    String department() default "";
}
