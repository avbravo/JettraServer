package io.jettra.server.backend.rest;

import io.jettra.server.backend.model.Order;
import io.jettra.server.backend.service.OrderService;
import io.jettra.server.openapi.annotations.ApiResponse;
import io.jettra.server.openapi.annotations.Operation;
import io.jettra.server.openapi.annotations.Parameter;
import io.jettra.server.openapi.annotations.Tag;
import io.jettra.rest.annotations.GET;
import io.jettra.rest.annotations.POST;
import io.jettra.rest.annotations.PUT;
import io.jettra.rest.annotations.Path;
import io.jettra.rest.annotations.PathParam;
import io.jettra.rest.annotations.accreditation.RolesAllowed;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Microservicio REST de Facturación y Órdenes de Venta.
 */
@Path("/api/v1/orders")
@Tag(name = "Microservicio de Órdenes", description = "Gestión de pedidos, facturación y cancelaciones")
public class OrderResource {

    @Inject
    private OrderService orderService;

    public OrderResource() {
        this(new OrderService());
    }

    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @GET
    @RolesAllowed({"ADMIN", "MANAGER", "USER"})
    @Operation(summary = "Listar todas las órdenes", description = "Retorna el historial completo de órdenes de venta.")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes")
    public void listOrders(HttpRequest req, HttpResponse res) {
        List<Order> orders = orderService.findAll();
        res.json(orders);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "MANAGER", "USER"})
    @Operation(summary = "Buscar orden por ID", description = "Obtiene los detalles de una orden específica.")
    @ApiResponse(responseCode = "200", description = "Orden encontrada")
    @ApiResponse(responseCode = "404", description = "Orden no existe")
    public void getOrder(@Parameter(description = "Identificador de orden (ej. ORD-1001)") @PathParam("id") String orderId,
                         HttpRequest req, HttpResponse res) {
        Optional<Order> order = orderService.findById(orderId);
        if (order.isPresent()) {
            res.json(order.get());
        } else {
            res.status(404).json(Map.of("error", "Orden no encontrada: " + orderId));
        }
    }

    @POST
    @RolesAllowed({"ADMIN", "MANAGER", "USER"})
    @Operation(summary = "Crear nueva orden", description = "Registra una orden de compra o servicio.")
    @ApiResponse(responseCode = "201", description = "Orden creada exitosamente")
    public void createOrder(HttpRequest req, HttpResponse res) {
        try {
            Map<?, ?> body = req.getBodyAsJson(Map.class);
            if (body == null) {
                res.status(400).json(Map.of("error", "Cuerpo JSON requerido"));
                return;
            }

            Long customerId = body.get("customerId") != null ? Long.parseLong(body.get("customerId").toString()) : 1L;
            String customerName = body.get("customerName") != null ? body.get("customerName").toString() : "Cliente General";
            double amount = body.get("totalAmount") != null ? Double.parseDouble(body.get("totalAmount").toString()) : 100.0;
            String currency = body.get("currency") != null ? body.get("currency").toString() : "USD";

            Order created = orderService.createOrder(customerId, customerName, amount, currency);
            res.status(201).json(created);
        } catch (Exception e) {
            res.status(400).json(Map.of("error", "Error procesando orden: " + e.getMessage()));
        }
    }

    @PUT
    @Path("/{id}/cancel")
    @RolesAllowed({"ADMIN", "MANAGER"})
    @Operation(summary = "Cancelar orden", description = "Cancela una orden existente. Requiere rol ADMIN o MANAGER.")
    @ApiResponse(responseCode = "200", description = "Orden cancelada")
    @ApiResponse(responseCode = "404", description = "Orden no existe")
    public void cancelOrder(@Parameter(description = "ID de la orden a cancelar") @PathParam("id") String orderId,
                            HttpRequest req, HttpResponse res) {
        boolean ok = orderService.cancelOrder(orderId);
        if (ok) {
            res.json(Map.of("message", "Orden cancelada exitosamente", "orderId", orderId));
        } else {
            res.status(404).json(Map.of("error", "Orden no encontrada para cancelación: " + orderId));
        }
    }
}

