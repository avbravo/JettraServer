# Soporte OpenAPI (Swagger UI) en JettraServer

JettraServer incluye soporte nativo para generar documentación OpenAPI v3 de tus endpoints y proveer una interfaz visual mediante **Swagger UI**.

## Configuración y Registro

A diferencia de los Handlers normales, los controladores REST con soporte para OpenAPI requieren que le proveas a `JettraServer` la lista de clases a escanear, para que pueda generar el documento JSON correspondiente de manera dinámica.

Para habilitar OpenAPI, registra `OpenApiHandler` y `SwaggerUIHandler` en tu aplicación (por ejemplo en tu método `main`):

```java
import io.jettra.server.JettraServer;
import io.jettra.server.openapi.OpenApiHandler;
import io.jettra.server.openapi.SwaggerUIHandler;
import com.jettra.example.controller.library.AuthorController;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        JettraServer server = new JettraServer();
        
        // Define la lista de controladores REST que deseas documentar
        List<Class<?>> controllers = List.of(
            AuthorController.class
            // Añade aquí más controladores
        );
        
        // Exponer el JSON de OpenAPI
        server.addHandler("/openapi.json", new OpenApiHandler(controllers));
        
        // Exponer la interfaz Swagger UI
        server.addHandler("/swagger-ui", new SwaggerUIHandler("/openapi.json"));
        
        server.start();
    }
}
```

Al iniciar tu servidor, puedes navegar a `http://localhost:8080/swagger-ui` (ajustando el puerto de tu aplicación) para ver la documentación interactiva.

## Anotaciones Soportadas

JettraServer usa las anotaciones de enrutamiento ubicadas en `com.jettra.rest.annotations` (`@Path`, `@GET`, `@POST`, `@PathParam`, `@QueryParam`, `@HeaderParam`) para deducir la estructura base.

Además, en el paquete `io.jettra.server.openapi.annotations` se proveen anotaciones extra para enriquecer el documento:

- `@OpenApi(title = "...", version = "...", description = "...")`: Anotación a nivel de clase para información general de la API.
- `@Operation(summary = "...", description = "...")`: Anota los métodos para describir la operación específica.
- `@ApiResponse(responseCode = "...", description = "...")`: Define múltiples respuestas posibles por método.
- `@Parameter(name = "...", description = "...", required = true)`: Agrega semántica adicional a un parámetro ya anotado con `@QueryParam` o afines.
- `@Tag(name = "...", description = "...")`: Anotación a nivel de clase o método para agrupar operaciones bajo un mismo tag (puede ser repetible).
- `@RequestBody(description = "...", required = true)`: Anotación para parámetros que representan el cuerpo de la petición.
- `@Schema(description = "...", example = "...")`: Anota los parámetros para enriquecer el esquema con descripciones y ejemplos.

### Ejemplo de Controlador Documentado

```java
import com.jettra.rest.annotations.*;
import io.jettra.server.openapi.annotations.*;

@OpenApi(title = "Librería API", version = "v1.0")
@Path("/api/authors")
public class AuthorController {

    @GET
    @Operation(summary = "Obtener Autores", description = "Lista todos los autores registrados")
    @ApiResponse(responseCode = "200", description = "Lista devuelta con éxito")
    public List<Author> findAll() {
        return AuthorRepository.findAll();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Borrar Autor")
    @ApiResponse(responseCode = "200", description = "Borrado exitosamente")
    @ApiResponse(responseCode = "404", description = "Autor no encontrado")
    public Response delete(
        @PathParam("id") 
        @Parameter(description = "ID único del autor") String id) {
        
        AuthorRepository.delete(id);
        return Response.ok("Deleted successfully").build();
    }
}
```
