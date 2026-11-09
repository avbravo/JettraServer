package io.jettra.aura.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contribución declarativa de metadatos, hojas de estilo CSS o scripts JS a la cabecera &lt;head&gt;.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuraHeader {
    String title() default "";
    String[] stylesheets() default {};
    String[] scripts() default {};
}

