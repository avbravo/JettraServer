# JettraRest - Comprehensive Guide & Architecture Manual

## 1. Overview & Architecture
`JettraRest` is an asynchronous, annotation-driven REST and HTTP micro-framework built on Java 25 Virtual Threads. It simplifies the definition, routing, serialization, and validation of RESTful web APIs across Jettra services.

---

## 2. Key Features
- **Declarative HTTP Annotations**: `@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE`, `@PathParam`, `@QueryParam`, `@Body`.
- **Integrated JSON Content Negotiation**: Direct binding with `JettraJSON` for request/response payloads.
- **Virtual Thread Dispatching**: Every incoming HTTP request executes on an ultra-lightweight virtual thread.
- **Filter and Interceptor Pipelines**: Built-in hooks for authentication, CORS, rate limiting, and structured logging.

---

## 3. Installation
```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>JettraRest</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. Usage & Code Examples

### 4.1 Defining a REST Resource
```java
package com.example.api;

import io.jettra.rest.annotation.*;
import io.jettra.rest.response.RestResponse;

@Path("/api/v1/customers")
public class CustomerResource {

    public record CustomerDto(String id, String name, String email) {}

    @GET
    @Path("/{id}")
    public RestResponse getCustomer(@PathParam("id") String id) {
        CustomerDto customer = new CustomerDto(id, "Carlos Mendez", "carlos@example.com");
        return RestResponse.ok(customer);
    }

    @POST
    public RestResponse createCustomer(@Body CustomerDto body) {
        System.out.println("Created customer: " + body.name());
        return RestResponse.created(body);
    }
}
```

### 4.2 Generating REST Resources at Compile Time with `@RestRepository` (`RF-22`)

Instead of writing boilerplate REST controllers manually, annotate a `@Repository` interface with `@RestRepository`. During `javac` compilation, `RestRepositoryProcessor` automatically generates the corresponding `<Name>Rest` resource class (e.g., `ProductRepository` -> `ProductRest`), mapping every method to HTTP verbs, path/query/body parameters, and JettraJWT declarative security.

#### Repository Interface
```java
package com.example.repository;

import io.jettra.rest.annotations.DenyAll;
import io.jettra.rest.annotations.PermitAll;
import io.jettra.rest.annotations.Repository;
import io.jettra.rest.annotations.RestRepository;
import io.jettra.rest.annotations.accreditation.RolesAllowed;
import java.util.List;

@Repository
@RestRepository(
    path = "/api/v1/products",
    secured = true,
    rolesAllowed = {"ADMIN", "USER"},
    declareRoles = {"ADMIN", "USER"}
)
public interface ProductRepository {

    // -> @GET @Path("/{id}") with @PathParam("id")
    Persona findById(String id);

    // -> @GET with @PermitAll (public endpoint)
    @PermitAll
    List<Persona> findAll();

    // -> @POST with JSON body and @RolesAllowed({"ADMIN"})
    @RolesAllowed({"ADMIN"})
    Persona save(Persona persona);

    // -> @PUT @Path("/{id}") with @PathParam("id") + JSON body
    @RolesAllowed({"ADMIN"})
    Persona update(String id, Persona persona);

    // -> @DELETE @Path("/{id}") with @PathParam("id")
    @RolesAllowed({"ADMIN"})
    boolean deleteById(String id);

    // -> @GET @Path("/search-by-name") with @QueryParam("name")
    List<Persona> searchByName(String name);

    // -> @DenyAll (HTTP 403)
    @DenyAll
    void purgeInternal();
}
```

#### Generated Resource (`ProductRest.java` at compile time)
```java
@Path("/api/v1/products")
@Produces("application/json")
@Consumes("application/json")
@Secured
@DeclareRoles({"ADMIN", "USER"})
@RolesAllowed({"ADMIN", "USER"})
public class ProductRest {

    @Inject
    private ProductRepository repository;

    public ProductRest() {}

    public ProductRest(ProductRepository repository) {
        this.repository = repository;
    }

    @GET
    @Path("/{id}")
    public Persona findById(@PathParam("id") String id) {
        return this.repository.findById(id);
    }
    // ...remaining endpoints generated automatically
}
```

#### Maven Compiler Configuration (Java 25+)
Ensure `-parameters` is enabled so parameter names (`id`, `name`, etc.) are preserved for `@PathParam` and `@QueryParam` generation:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <release>25</release>
        <parameters>true</parameters>
        <annotationProcessorPaths>
            <path>
                <groupId>io.jettra</groupId>
                <artifactId>JettraRest</artifactId>
                <version>1.0.0-SNAPSHOT</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```
