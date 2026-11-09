package io.jettra.aura.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permite vincular declarativamente un campo de la clase Java con el componente o etiqueta
 * identificada por aura:id en la plantilla HTML.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface AuraComponent {
    String value() default "";
}

