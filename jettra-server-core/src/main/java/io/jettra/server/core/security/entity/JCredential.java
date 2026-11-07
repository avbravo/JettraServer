package io.jettra.server.core.security.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad inmutable que representa las Credenciales de acceso de un Usuario en JettraSecurityDB.
 */
public record JCredential(
        UUID id,
        JUser jUser,
        String username,
        String passwordHash,
        Boolean active,
        Instant lastLogin
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public JCredential {
        Objects.requireNonNull(username, "El username es obligatorio");
        Objects.requireNonNull(passwordHash, "El hash de contraseña es obligatorio");
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (active == null) {
            active = true;
        }
    }
}

