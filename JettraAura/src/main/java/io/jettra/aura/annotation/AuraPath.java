package io.jettra.aura.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define la ruta URI montada en el servidor para acceder a la página Aura.
 * Soporta comodines y parámetros de ruta (ej. "/users/{id}").
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuraPath {
    String value();
    int order() default 0;
}

