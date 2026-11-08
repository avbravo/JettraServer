package io.jettra.flux.security;

import java.util.Set;

/**
 * Sealed interface representing the outcome of a page or action security evaluation.
 * Leverages Java 25 pattern matching and records.
 */
public sealed interface SecurityDecision {

    record Granted() implements SecurityDecision {}

    record RedirectToLogin(String loginPath) implements SecurityDecision {}

    record Denied(int statusCode, String reason, Set<String> requiredRoles) implements SecurityDecision {}
}
