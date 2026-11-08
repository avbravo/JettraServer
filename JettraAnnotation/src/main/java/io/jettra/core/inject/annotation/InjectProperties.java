package io.jettra.core.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inyecta un archivo de propiedades en un campo de una Page.
 * Busca automáticamente archivos con el formato name_{lang}.properties.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectProperties {
    /**
     * El nombre base del archivo de propiedades (sin _lang.properties).
     * Por defecto es "messages".
     */
    String name() default "messages";
}
