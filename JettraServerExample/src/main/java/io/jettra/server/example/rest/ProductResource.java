package io.jettra.server.example.rest;

import io.jettra.server.example.model.Product;
import io.jettra.server.example.service.ProductService;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;
import io.jettra.server.jakarta.rest.RestAnnotations.Consumes;
import io.jettra.server.jakarta.rest.RestAnnotations.DELETE;
import io.jettra.server.jakarta.rest.RestAnnotations.GET;
import io.jettra.server.jakarta.rest.RestAnnotations.POST;
import io.jettra.server.jakarta.rest.RestAnnotations.Path;
import io.jettra.server.jakarta.rest.RestAnnotations.PathParam;
import io.jettra.server.jakarta.rest.RestAnnotations.Produces;
import java.util.List;
import java.util.Map;

/**
 * Recurso RESTful para la integración API de productos.
 */
@Path("/api/productos")
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

    @Inject
    private ProductService productService;

    public ProductResource() {}

    public ProductResource(ProductService productService) {
        this.productService = productService;
    }

    @GET
    public List<Product> listarTodos() {
        return productService != null ? productService.listAll() : List.of();
    }

    @GET
    @Path("/{id}")
    public Product obtenerPorId(@PathParam("id") String id) {
        return productService != null ? productService.findById(id) : null;
    }

    @POST
    public Map<String, Object> crear(Product p) {
        if (productService != null && p != null) {
            Product saved = productService.save(p);
            return Map.of("status", "SUCCESS", "message", "Producto creado", "data", saved);
        }
        return Map.of("status", "ERROR", "message", "No se pudo crear el producto");
    }

    @DELETE
    @Path("/{id}")
    public Map<String, Object> eliminar(@PathParam("id") String id) {
        if (productService != null && id != null) {
            boolean ok = productService.delete(id);
            return Map.of("status", ok ? "SUCCESS" : "NOT_FOUND", "deletedId", id);
        }
        return Map.of("status", "ERROR");
    }
}

