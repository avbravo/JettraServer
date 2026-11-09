package io.jettra.aura.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación de seguridad para páginas o componentes Aura que requieren autenticación
 * o roles específicos basados en RBAC (JettraSecurityDB / JWT).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AuraAuthorize {
    String[] roles() default {};
    String redirectOnDenied() default "/login";
}

