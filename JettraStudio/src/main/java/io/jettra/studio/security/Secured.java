package io.jettra.studio.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares security constraints for a JettraStudio WebPage.
 * Requires user authentication and optionally enforces authorized roles.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Secured {
    /**
     * Allowed roles to access this page. If empty, any authenticated user is allowed.
     */
    String[] roles() default {};

    /**
     * Login redirection URL when user is not authenticated.
     */
    String loginUrl() default "/login";
}
