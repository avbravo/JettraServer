package io.jettra.server.core.security.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Entidad inmutable que representa un Usuario dentro de JettraSecurityDB.
 */
public record JUser(
        UUID id,
        String firstName, // Usado como nombre de usuario principal (username)
        String lastName,
        String email,
        String phone,
        Boolean active,
        Set<JRole> jRoles,
        Set<String> assignedDatabases
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public JUser {
        Objects.requireNonNull(firstName, "El firstName (username) es obligatorio");
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (active == null) {
            active = true;
        }

        if (lastName == null || lastName.isBlank()) {
            if (assignedDatabases != null && !assignedDatabases.isEmpty()) {
                lastName = String.join(", ", assignedDatabases);
            } else {
                lastName = "*";
            }
        }

        if (assignedDatabases == null) {
            if (lastName != null && !lastName.isBlank() && !lastName.equalsIgnoreCase("None")) {
                Set<String> dbs = new TreeSet<>();
                for (String db : lastName.split(",")) {
                    String trimmed = db.trim();
                    if (!trimmed.isEmpty()) {
                        dbs.add(trimmed);
                    }
                }
                assignedDatabases = Collections.unmodifiableSet(dbs);
            } else {
                assignedDatabases = Collections.emptySet();
            }
        } else {
            Set<String> dbs = new TreeSet<>();
            for (String db : assignedDatabases) {
                if (db != null && !db.trim().isEmpty()) {
                    dbs.add(db.trim());
                }
            }
            assignedDatabases = Collections.unmodifiableSet(dbs);
        }

        if (jRoles == null) {
            jRoles = Collections.emptySet();
        } else {
            Set<JRole> sortedRoles = new TreeSet<>(jRoles);
            jRoles = Collections.unmodifiableSet(sortedRoles);
        }
    }

    public JUser(UUID id, String firstName, String lastName, String email, String phone, Boolean active, Set<JRole> jRoles) {
        this(id, firstName, lastName, email, phone, active, jRoles, null);
    }

    public String username() {
        return firstName;
    }
}
