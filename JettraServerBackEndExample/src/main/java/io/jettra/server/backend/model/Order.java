package io.jettra.server.backend.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Entidad de dominio Orden / Pedido para microservicio de facturación y pedidos.
 */
public record Order(
        String orderId,
        Long customerId,
        String customerName,
        double totalAmount,
        String currency,
        String status,
        Instant createdAt
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

