package io.jettra.server.core.security.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad inmutable que representa un Rol del sistema dentro de JettraSecurityDB.
 */
public record JRole(
        UUID id,
        String name,
        Boolean active
) implements Serializable, Comparable<JRole> {
    private static final long serialVersionUID = 1L;

    public JRole {
        Objects.requireNonNull(name, "El nombre del rol es obligatorio");
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (active == null) {
            active = true;
        }
    }

    public static JRole of(String name) {
        return new JRole(UUID.nameUUIDFromBytes(name.toUpperCase().getBytes(java.nio.charset.StandardCharsets.UTF_8)), name.toUpperCase(), true);
    }

    @Override
    public int compareTo(JRole o) {
        if (o == null || o.name() == null) return 1;
        if (this.name == null) return -1;
        return this.name.compareToIgnoreCase(o.name());
    }
}
