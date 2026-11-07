package io.jettra.server.backend.rest;

import io.jettra.server.backend.model.Customer;
import io.jettra.server.backend.service.CustomerService;
import io.jettra.server.openapi.annotations.ApiResponse;
import io.jettra.server.openapi.annotations.Operation;
import io.jettra.server.openapi.annotations.Parameter;
import io.jettra.server.openapi.annotations.Tag;
import io.jettra.rest.annotations.DELETE;
import io.jettra.rest.annotations.GET;
import io.jettra.rest.annotations.POST;
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
 * Microservicio REST de Gestión de Clientes.
 * Protegido mediante Tokens JWT Bearer emitidos por JettraSecurityDB.
 */
@Path("/api/v1/customers")
@Tag(name = "Microservicio de Clientes", description = "Operaciones CRUD para clientes y cuentas empresariales")
public class CustomerResource {

    @Inject
    private CustomerService customerService;

    public CustomerResource() {
        this(new CustomerService());
    }

    public CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GET
    @RolesAllowed({"ADMIN", "MANAGER", "USER"})
    @Operation(summary = "Listar todos los clientes", description = "Requiere token JWT válido con roles ADMIN, MANAGER o USER.")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    @ApiResponse(responseCode = "401", description = "Token JWT no provisto o inválido")
    public void listCustomers(HttpRequest req, HttpResponse res) {
        List<Customer> customers = customerService.findAll();
        res.json(customers);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "MANAGER", "USER"})
    @Operation(summary = "Obtener cliente por ID", description = "Retorna los detalles del cliente si existe.")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    public void getCustomer(@Parameter(description = "Identificador único del cliente") @PathParam("id") String idStr,
                            HttpRequest req, HttpResponse res) {
        try {
            Long id = Long.parseLong(idStr);
            Optional<Customer> cust = customerService.findById(id);
            if (cust.isPresent()) {
                res.json(cust.get());
            } else {
                res.status(404).json(Map.of("error", "Cliente no encontrado para ID: " + id));
            }
        } catch (NumberFormatException e) {
            res.status(400).json(Map.of("error", "ID numérico inválido"));
        }
    }

    @POST
    @RolesAllowed({"ADMIN", "MANAGER"})
    @Operation(summary = "Crear nuevo cliente", description = "Registra un cliente. Requiere rol ADMIN o MANAGER.")
    @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de cliente incompletos o erróneos")
    public void createCustomer(HttpRequest req, HttpResponse res) {
        try {
            Customer payload = req.getBodyAsJson(Customer.class);
            if (payload == null || payload.name() == null || payload.name().isBlank()) {
                res.status(400).json(Map.of("error", "El nombre del cliente es obligatorio"));
                return;
            }
            Customer saved = customerService.save(payload);
            res.status(201).json(saved);
        } catch (Exception e) {
            res.status(400).json(Map.of("error", "Error procesando cuerpo JSON: " + e.getMessage()));
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente. Requiere exclusivamente rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Cliente eliminado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    public void deleteCustomer(@Parameter(description = "ID del cliente a eliminar") @PathParam("id") String idStr,
                               HttpRequest req, HttpResponse res) {
        try {
            Long id = Long.parseLong(idStr);
            boolean ok = customerService.delete(id);
            if (ok) {
                res.json(Map.of("message", "Cliente eliminado con éxito", "id", id));
            } else {
                res.status(404).json(Map.of("error", "Cliente no encontrado para ID: " + id));
            }
        } catch (NumberFormatException e) {
            res.status(400).json(Map.of("error", "ID numérico inválido"));
        }
    }
}

