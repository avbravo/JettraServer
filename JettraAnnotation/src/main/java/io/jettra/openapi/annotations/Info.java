package io.jettra.openapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Info {
    String title() default "";
    String version() default "";
    String description() default "";
    String termsOfService() default "";
}
