package io.jettra.core.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para convertir automáticamente un Modelo en un Record y viceversa.
 * El procesador de anotaciones generará una clase Converter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page {
    /**
     * El nombre de la clase Record destino. Si no se especifica,
     * se asume el mismo nombre del modelo eliminando "Model.java".
     */
    String  path() default "/";
}
