package io.jettra.flux.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica cómo mapear el atributo del modelo en el record.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertiesInRecord {
    /**
     * Nombre del campo correspondiente en el Record.
     * Si está vacío, se asume que usa el mismo nombre del atributo.
     */
    String field() default "";
}
