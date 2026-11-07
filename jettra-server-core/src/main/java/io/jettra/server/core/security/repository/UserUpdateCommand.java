package io.jettra.server.core.security.repository;

import io.jettra.server.core.security.entity.JRole;
import java.util.Set;

/**
 * Comando para actualización atómica de atributos de usuario en JettraSecurityDB.
 */
public record UserUpdateCommand(
        String email,
        String phone,
        Boolean active,
        Set<JRole> roles,
        Set<String> assignedDatabases
) {}

