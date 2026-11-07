package io.jettra.server.backend.model;

import java.io.Serializable;

/**
 * Entidad de dominio Cliente para microservicio de clientes.
 */
public record Customer(
        Long id,
        String name,
        String email,
        String company,
        String tier,
        boolean active
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

