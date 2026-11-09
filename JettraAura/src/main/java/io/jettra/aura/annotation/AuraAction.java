package io.jettra.aura.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Vincula un método ejecutor de acción en la clase Java con un botón, enlace o evento
 * con el aura:id especificado.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuraAction {
    String value() default "";
}

