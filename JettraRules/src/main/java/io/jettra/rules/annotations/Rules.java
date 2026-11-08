package io.jettra.rules.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define business rules for a field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Rules {
    /**
     * Optional field name if applying to class level or for reference.
     */
    String field() default "";
    
    /**
     * The operator or rule to apply (e.g., "lessorequals", "greater", "equals").
     */
    String apply() default "";
    
    /**
     * The value or field name to compare against.
     */
    String than() default "";
    
    /**
     * Custom message to show when the rule is violated.
     */
    String message() default "";
}
