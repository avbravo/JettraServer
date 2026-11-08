package jcf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jcf.AppRole;

/**
 * Declarative role-based access control annotation for JettraFlux page widgets.
 * Defines the minimum roles and optional department required to access and render the page.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PageWidgetAllow {
    AppRole[] role() default {};
    String department() default "";
}
