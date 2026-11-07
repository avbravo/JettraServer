package io.jettra.server.backend.service;

import io.jettra.server.backend.model.Order;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio para gestión y ciclo de vida de Órdenes / Pedidos.
 */
@ApplicationScoped
public class OrderService {

    private final Map<String, Order> orderMap = new ConcurrentHashMap<>();
    private final AtomicLong orderSeq = new AtomicLong(1000);

    public OrderService() {
        initSampleData();
    }

    private void initSampleData() {
        createOrder(1L, "Acme Corporation", 12500.0, "USD");
        createOrder(2L, "Stark Industries", 89400.0, "USD");
        createOrder(3L, "Wayne Enterprises", 45200.0, "EUR");
    }

    public List<Order> findAll() {
        return new ArrayList<>(orderMap.values());
    }

    public Optional<Order> findById(String orderId) {
        if (orderId == null) return Optional.empty();
        return Optional.ofNullable(orderMap.get(orderId));
    }

    public Order createOrder(Long customerId, String customerName, double amount, String currency) {
        String id = "ORD-" + orderSeq.incrementAndGet();
        Order order = new Order(
                id,
                customerId,
                customerName != null ? customerName : "Cliente General",
                amount,
                currency != null ? currency : "USD",
                "CONFIRMED",
                Instant.now()
        );
        orderMap.put(id, order);
        return order;
    }

    public boolean cancelOrder(String orderId) {
        Order existing = orderMap.get(orderId);
        if (existing == null) return false;
        orderMap.put(orderId, new Order(
                existing.orderId(),
                existing.customerId(),
                existing.customerName(),
                existing.totalAmount(),
                existing.currency(),
                "CANCELLED",
                existing.createdAt()
        ));
        return true;
    }

    public long count() {
        return orderMap.size();
    }
}

