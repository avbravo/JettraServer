package io.jettra.flux.security;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Immutable Java 25 record representing an authenticated security principal.
 * Holds identity, assigned roles, department context, and multi-database authorization scopes.
 */
public record SecurityPrincipal(
    String username,
    Set<String> roles,
    String department,
    Set<String> assignedDatabases
) {
    public SecurityPrincipal {
        Objects.requireNonNull(username, "username must not be null");
        roles = (roles == null) ? Set.of() : Set.copyOf(roles);
        department = (department == null) ? "" : department.trim();
        assignedDatabases = (assignedDatabases == null) ? Set.of() : Set.copyOf(assignedDatabases);
    }

    /**
     * Backwards-compatible 3-argument constructor defaulting assignedDatabases to empty set.
     */
    public SecurityPrincipal(String username, Set<String> roles, String department) {
        this(username, roles, department, Set.of());
    }

    /**
     * Factory method creating a SecurityPrincipal from comma-separated roles or single role.
     */
    public static SecurityPrincipal of(String username, String role, String department) {
        return of(username, role, department, Set.of());
    }

    /**
     * Factory method creating a SecurityPrincipal with explicit multi-database scope.
     */
    public static SecurityPrincipal of(String username, String role, String department, Set<String> assignedDatabases) {
        Set<String> roleSet = (role == null || role.isBlank())
            ? Set.of()
            : Arrays.stream(role.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        return new SecurityPrincipal(username, roleSet, department, assignedDatabases);
    }

    /**
     * Factory method creating a SecurityPrincipal from comma-separated roles and comma-separated database scopes.
     */
    public static SecurityPrincipal of(String username, String role, String department, String databaseCsv) {
        Set<String> dbSet = (databaseCsv == null || databaseCsv.isBlank())
            ? Set.of()
            : Arrays.stream(databaseCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        return of(username, role, department, dbSet);
    }

    public boolean hasRole(String targetRole) {
        if (targetRole == null || targetRole.isBlank()) {
            return false;
        }
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(targetRole.trim()));
    }

    public boolean hasAnyRole(Set<String> targetRoles) {
        if (targetRoles == null || targetRoles.isEmpty()) {
            return true;
        }
        return targetRoles.stream().anyMatch(this::hasRole);
    }

    /**
     * Verifies whether this principal is authorized to perform operations against the target database.
     */
    public boolean isAuthorizedForDatabase(String dbName) {
        if (dbName == null || dbName.isBlank()) {
            return false;
        }

        // Global admin roles possess unrestricted wildcard access across all databases
        if (hasRole("ADMIN") || hasRole("ROLE_ADMIN") || hasRole("SUPERADMIN") ||
            "admin".equalsIgnoreCase(username) || "root".equalsIgnoreCase(username)) {
            return true;
        }

        // Department wildcard or department database match
        if (!department.isEmpty()) {
            if ("*".equals(department) || department.equalsIgnoreCase(dbName.trim())) {
                return true;
            }
        }

        // Database-specific roles
        String upperDb = dbName.trim().toUpperCase();
        if (hasRole("DB_" + upperDb) || hasRole("READ_" + upperDb) || hasRole("ADMIN_" + upperDb)) {
            return true;
        }

        // Explicit assignedDatabases match or wildcard
        if (assignedDatabases.contains("*") || assignedDatabases.contains(dbName)) {
            return true;
        }
        return assignedDatabases.stream().anyMatch(d -> d.equalsIgnoreCase(dbName.trim()));
    }
}
