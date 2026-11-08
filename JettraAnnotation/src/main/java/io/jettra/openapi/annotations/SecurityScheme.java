package io.jettra.openapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SecuritySchemes.class)
@Inherited
public @interface SecurityScheme {
    String name() default "";
    String type() default "http";
    String description() default "";
    String scheme() default "bearer";
    String bearerFormat() default "JWT";
    String in() default "header";
}
