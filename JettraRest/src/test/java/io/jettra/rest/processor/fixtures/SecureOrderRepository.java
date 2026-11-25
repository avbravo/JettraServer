package io.jettra.rest.processor.fixtures;

import io.jettra.rest.annotations.Repository;
import io.jettra.rest.annotations.RestRepository;

@Repository
@RestRepository(
        path = "/api/orders",
        resourceName = "OrderApiRest",
        secured = true,
        rolesAllowed = {"ADMIN", "MANAGER"},
        declareRoles = {"ADMIN", "MANAGER", "AUDITOR"}
)
public interface SecureOrderRepository {

    Persona findById(String orderId);

    long count();
}
