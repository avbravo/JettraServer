package io.jettra.server.jakarta.security;

import io.jettra.server.core.http.HttpFilter;
import io.jettra.server.core.http.HttpStatus;
import io.jettra.server.core.security.JettraJwtAuthenticator;
import io.jettra.server.core.security.SecurityContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Set;

public final class SecurityAnnotations {
    private SecurityAnnotations() {}

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RolesAllowed {
        String[] value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PermitAll {}

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DenyAll {}

    public static HttpFilter createFilter(JettraJwtAuthenticator authenticator) {
        return (req, res, chain) -> {
            SecurityContext sec = authenticator.authenticate(req);
            req.setAttribute("securityContext", sec);
            chain.doFilter(req, res);
        };
    }

    public static boolean checkAccess(SecurityContext sec, Set<String> allowedRoles) {
        if (allowedRoles.isEmpty()) return true;
        for (String role : allowedRoles) {
            if (sec.hasRole(role)) return true;
        }
        return false;
    }
}
