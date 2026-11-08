package io.jettra.flux.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define labels for ViewModel attributes.
 * Allows mapping a field to a property key and providing a default label.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertiesLabel {
    /**
     * The key in the properties file.
     */
    String value();

    /**
     * The default label if the property key is not found.
     */
    String label() default "";
}
