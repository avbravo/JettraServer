package io.jettra.server.core.security;

import io.jettra.jwt.JettraJWT;
import io.jettra.server.core.http.HttpRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Autenticador basado en tokens JWT nativos de JettraJWT.
 */
public class JettraJwtAuthenticator {

    private final JettraJWT jwtEngine;

    public JettraJwtAuthenticator(String secret, long expirationMillis) {
        this.jwtEngine = new JettraJWT(secret, expirationMillis);
    }

    public SecurityContext authenticate(HttpRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return SecurityContext.anonymous();
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtEngine.validateToken(token)) {
            return SecurityContext.anonymous();
        }

        try {
            String subject = jwtEngine.extractUsername(token);
            Map<String, Object> claims = jwtEngine.getPayload(token);
            Set<String> roles = new HashSet<>(jwtEngine.extractRoles(token));

            return new SecurityContext(subject, roles, claims, true);
        } catch (Exception e) {
            return SecurityContext.anonymous();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Map<String, Object> claims) {
        if (claims == null) return Collections.emptySet();
        Object r = claims.get("roles");
        if (r instanceof List<?> list) {
            Set<String> set = new HashSet<>();
            for (Object item : list) {
                if (item != null) set.add(item.toString());
            }
            return set;
        } else if (r instanceof String s) {
            return Set.of(s.split(","));
        }
        return Collections.emptySet();
    }

    public JettraJWT getJwtEngine() {
        return jwtEngine;
    }
}
