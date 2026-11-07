package io.jettra.server.core.security.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad inmutable que representa un Privilegio o Acreditación concedida a un Rol en JettraSecurityDB.
 */
public record JAccreditation(
        UUID id,
        JRole jRole,
        String feature,
        Boolean active
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public JAccreditation {
        Objects.requireNonNull(feature, "El feature o permiso es obligatorio");
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (active == null) {
            active = true;
        }
    }
}

