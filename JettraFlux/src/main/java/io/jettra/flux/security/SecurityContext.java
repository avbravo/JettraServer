package io.jettra.flux.security;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable Java 25 record representing the security context of the current request/session.
 */
public record SecurityContext(
    SecurityPrincipal principal,
    boolean isAuthenticated
) {
    private static final SecurityContext UNAUTHENTICATED = new SecurityContext(null, false);

    public static SecurityContext unauthenticated() {
        return UNAUTHENTICATED;
    }

    public static SecurityContext authenticated(SecurityPrincipal principal) {
        return new SecurityContext(Objects.requireNonNull(principal, "principal must not be null"), true);
    }

    public Optional<SecurityPrincipal> getPrincipal() {
        return Optional.ofNullable(principal);
    }

    public boolean hasRole(String role) {
        return isAuthenticated && principal != null && principal.hasRole(role);
    }
}
