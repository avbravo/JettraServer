package io.jettra.rules.annotations;

import io.jettra.rules.enums.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to perform dynamic computations on fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Compute {
    /**
     * Primary operation for simple computations.
     */
    OperationType operation() default OperationType.NONE;
    
    /**
     * Fields involved in the operation.
     */
    String[] fields() default {};
    
    /**
     * Complex expression for nested operations, conditionals, and chaining.
     * Supports Syntax like: SUBTRACTION(f1, f2).APPLY(IF(f3 > 0).THEN(MULT(BEFORE, f3)))
     */
    String expression() default "";

    /**
     * Indicates if the field is editable in the UI.
     * Default is false.
     */
    boolean editable() default false;
}
