package io.jettra.flux.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify a method to handle a specific UI event, e.g., "submit", "click".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnEvent {
    /**
     * Name of the event to listen for.
     * @return Event name.
     */
    String name();
}
