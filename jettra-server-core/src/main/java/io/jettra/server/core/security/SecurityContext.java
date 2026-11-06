package io.jettra.server.core.security;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Contexto de seguridad de la petición actual, con información del usuario y roles.
 */
public record SecurityContext(
    String username,
    Set<String> roles,
    Map<String, Object> claims,
    boolean authenticated
) {
    public static SecurityContext anonymous() {
        return new SecurityContext("anonymous", Collections.emptySet(), Collections.emptyMap(), false);
    }

    public boolean hasRole(String role) {
        return authenticated && roles != null && roles.contains(role);
    }
}
