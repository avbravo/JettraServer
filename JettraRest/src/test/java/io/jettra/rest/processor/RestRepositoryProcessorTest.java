package io.jettra.rest.processor;

import io.jettra.rest.annotations.Consumes;
import io.jettra.rest.annotations.DELETE;
import io.jettra.rest.annotations.DenyAll;
import io.jettra.rest.annotations.GET;
import io.jettra.rest.annotations.POST;
import io.jettra.rest.annotations.PUT;
import io.jettra.rest.annotations.Path;
import io.jettra.rest.annotations.PathParam;
import io.jettra.rest.annotations.PermitAll;
import io.jettra.rest.annotations.Produces;
import io.jettra.rest.annotations.QueryParam;
import io.jettra.rest.annotations.Secured;
import io.jettra.rest.annotations.accreditation.DeclareRoles;
import io.jettra.rest.annotations.accreditation.RolesAllowed;
import io.jettra.rest.processor.fixtures.OrderApiRest;
import io.jettra.rest.processor.fixtures.Persona;
import io.jettra.rest.processor.fixtures.ProductRepository;
import io.jettra.rest.processor.fixtures.ProductRest;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class RestRepositoryProcessorTest {

    @Test
    @DisplayName("RF-22: Genera ProductRest en tiempo de compilación con rutas REST, parámetros y seguridad")
    public void testGeneratedProductRestMetadataAndSecurity() throws Exception {
        Class<?> clazz = ProductRest.class;

        // Anotaciones a nivel de clase
        assertTrue(clazz.isAnnotationPresent(Path.class), "ProductRest debe tener @Path");
        assertEquals("/product", clazz.getAnnotation(Path.class).value());
        assertTrue(clazz.isAnnotationPresent(Produces.class), "ProductRest debe tener @Produces");
        assertEquals("application/json", clazz.getAnnotation(Produces.class).value()[0]);
        assertTrue(clazz.isAnnotationPresent(Consumes.class), "ProductRest debe tener @Consumes");
        assertEquals("application/json", clazz.getAnnotation(Consumes.class).value()[0]);
        assertTrue(clazz.isAnnotationPresent(Secured.class), "ProductRest debe estar protegido con @Secured por defecto");

        // 1. findById(String id) -> @GET @Path("/{id}") con @PathParam("id")
        Method findById = clazz.getMethod("findById", String.class);
        assertTrue(findById.isAnnotationPresent(GET.class), "findById debe mapearse a @GET");
        assertEquals("/{id}", findById.getAnnotation(Path.class).value());
        Parameter idParam = findById.getParameters()[0];
        assertTrue(idParam.isAnnotationPresent(PathParam.class), "El parámetro id debe llevar @PathParam");
        assertEquals("id", idParam.getAnnotation(PathParam.class).value());

        // 2. findAll() -> @GET con @PermitAll propagado
        Method findAll = clazz.getMethod("findAll");
        assertTrue(findAll.isAnnotationPresent(GET.class), "findAll debe mapearse a @GET");
        assertTrue(findAll.isAnnotationPresent(PermitAll.class), "findAll debe propagar @PermitAll");

        // 3. save(Persona) -> @POST con @RolesAllowed({"ADMIN", "EDITOR"})
        Method save = clazz.getMethod("save", Persona.class);
        assertTrue(save.isAnnotationPresent(POST.class), "save debe mapearse a @POST");
        assertTrue(save.isAnnotationPresent(RolesAllowed.class), "save debe propagar @RolesAllowed");
        assertEquals(List.of("ADMIN", "EDITOR"), Arrays.asList(save.getAnnotation(RolesAllowed.class).value()));

        // 4. update(String id, Persona) -> @PUT @Path("/{id}") con @PathParam("id") y body Persona
        Method update = clazz.getMethod("update", String.class, Persona.class);
        assertTrue(update.isAnnotationPresent(PUT.class), "update debe mapearse a @PUT");
        assertEquals("/{id}", update.getAnnotation(Path.class).value());
        assertEquals("id", update.getParameters()[0].getAnnotation(PathParam.class).value());
        assertFalse(update.getParameters()[1].isAnnotationPresent(QueryParam.class), "El cuerpo Persona no debe ser @QueryParam");

        // 5. deleteById(String id) -> @DELETE @Path("/{id}") con @RolesAllowed({"ADMIN"})
        Method deleteById = clazz.getMethod("deleteById", String.class);
        assertTrue(deleteById.isAnnotationPresent(DELETE.class), "deleteById debe mapearse a @DELETE");
        assertEquals("/{id}", deleteById.getAnnotation(Path.class).value());
        assertEquals(List.of("ADMIN"), Arrays.asList(deleteById.getAnnotation(RolesAllowed.class).value()));

        // 6. searchByName(String name) -> @GET @Path("/search-by-name") con @QueryParam("name")
        Method search = clazz.getMethod("searchByName", String.class);
        assertTrue(search.isAnnotationPresent(GET.class), "searchByName debe mapearse a @GET");
        assertEquals("/search-by-name", search.getAnnotation(Path.class).value());
        assertEquals("name", search.getParameters()[0].getAnnotation(QueryParam.class).value());

        // 7. purgeInternal() -> @DenyAll propagado
        Method purge = clazz.getMethod("purgeInternal");
        assertTrue(purge.isAnnotationPresent(DenyAll.class), "purgeInternal debe propagar @DenyAll");
    }

    @Test
    @DisplayName("RF-22: ProductRest generado delega correctamente las llamadas al ProductRepository subyacente")
    public void testGeneratedProductRestDelegatesToRepository() {
        Map<String, Persona> store = new ConcurrentHashMap<>();
        store.put("P-1", new Persona("P-1", "Ana López", "ana@jettra.io"));

        ProductRepository repo = new ProductRepository() {
            @Override
            public Persona findById(String id) {
                return store.get(id);
            }

            @Override
            public List<Persona> findAll() {
                return List.copyOf(store.values());
            }

            @Override
            public Persona save(Persona persona) {
                store.put(persona.id(), persona);
                return persona;
            }

            @Override
            public Persona update(String id, Persona persona) {
                store.put(id, persona);
                return persona;
            }

            @Override
            public boolean deleteById(String id) {
                return store.remove(id) != null;
            }

            @Override
            public List<Persona> searchByName(String name) {
                return store.values().stream()
                        .filter(p -> p.nombre().toLowerCase().contains(name.toLowerCase()))
                        .toList();
            }

            @Override
            public void purgeInternal() {
                store.clear();
            }
        };

        ProductRest rest = new ProductRest(repo);
        assertEquals("Ana López", rest.findById("P-1").nombre());
        assertEquals(1, rest.findAll().size());

        rest.save(new Persona("P-2", "Carlos Ruiz", "carlos@jettra.io"));
        assertEquals(2, rest.findAll().size());
        assertEquals(1, rest.searchByName("carlos").size());

        rest.update("P-2", new Persona("P-2", "Carlos R. Actualizado", "carlos@jettra.io"));
        assertEquals("Carlos R. Actualizado", rest.findById("P-2").nombre());

        assertTrue(rest.deleteById("P-2"));
        assertNull(rest.findById("P-2"));
    }

    @Test
    @DisplayName("RF-22: Soporta path personalizado, resourceName, @RolesAllowed y @DeclareRoles a nivel de clase")
    public void testCustomSecureOrderRestConfiguration() throws Exception {
        Class<?> clazz = OrderApiRest.class;
        assertEquals("/api/orders", clazz.getAnnotation(Path.class).value());
        assertTrue(clazz.isAnnotationPresent(Secured.class));
        assertEquals(List.of("ADMIN", "MANAGER"), Arrays.asList(clazz.getAnnotation(RolesAllowed.class).value()));
        assertEquals(List.of("ADMIN", "MANAGER", "AUDITOR"), Arrays.asList(clazz.getAnnotation(DeclareRoles.class).value()));

        Method findById = clazz.getMethod("findById", String.class);
        assertEquals("/{orderId}", findById.getAnnotation(Path.class).value());
        assertEquals("orderId", findById.getParameters()[0].getAnnotation(PathParam.class).value());

        Method count = clazz.getMethod("count");
        assertTrue(count.isAnnotationPresent(GET.class));
        assertEquals("/count", count.getAnnotation(Path.class).value());
    }

    @Test
    @DisplayName("RF-22: Validación en tiempo de compilación rechaza @RestRepository sin @Repository o sobre clases")
    public void testCompileTimeValidationErrorsAndCustomRepositoryAnnotation() throws Exception {
        // 1. Sin @Repository -> error de compilación
        CompilationResult missingRepo = compileWithProcessor(
                "demo.InvalidMissingRepo",
                """
                package demo;
                import io.jettra.rest.annotations.RestRepository;
                @RestRepository
                public interface InvalidMissingRepo {
                    String findById(String id);
                }
                """
        );
        assertFalse(missingRepo.success(), "Debe fallar si falta @Repository");
        assertTrue(missingRepo.messages().contains("@Repository"),
                "El mensaje de error debe indicar que falta @Repository: " + missingRepo.messages());

        // 2. Sobre una clase concreta -> error de compilación
        CompilationResult onClass = compileWithProcessor(
                "demo.InvalidClassRepo",
                """
                package demo;
                import io.jettra.rest.annotations.Repository;
                import io.jettra.rest.annotations.RestRepository;
                @Repository
                @RestRepository
                public class InvalidClassRepo {
                }
                """
        );
        assertFalse(onClass.success(), "Debe fallar si @RestRepository se aplica sobre una clase");
        assertTrue(onClass.messages().contains("interfaces"),
                "El mensaje debe indicar que solo aplica sobre interfaces: " + onClass.messages());

        // 3. Con una anotación @Repository externa (ej. Jakarta Data / propia) -> compila y genera CustomerRest
        CompilationResult customRepo = compileWithProcessor(
                Map.of(
                        "ext.Repository",
                        """
                        package ext;
                        import java.lang.annotation.*;
                        @Target(ElementType.TYPE)
                        @Retention(RetentionPolicy.RUNTIME)
                        public @interface Repository {}
                        """,
                        "demo.CustomerRepository",
                        """
                        package demo;
                        import ext.Repository;
                        import io.jettra.rest.annotations.RestRepository;
                        @Repository
                        @RestRepository(secured = false)
                        public interface CustomerRepository {
                            String findById(String id);
                        }
                        """
                )
        );
        assertTrue(customRepo.success(), "Debe compilar con cualquier @Repository externo: " + customRepo.messages());
        assertTrue(customRepo.generatedClasses().contains("demo.CustomerRest"),
                "Debe haber generado demo.CustomerRest en tiempo de compilación");
    }

    private record CompilationResult(boolean success, String messages, List<String> generatedClasses) {
    }

    private static CompilationResult compileWithProcessor(String className, String source) throws Exception {
        return compileWithProcessor(Map.of(className, source));
    }

    private static CompilationResult compileWithProcessor(Map<String, String> sources) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Debe existir JavaCompiler del JDK 25");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        java.nio.file.Path outDir = Files.createTempDirectory("jettra_rest_proc_test_");
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null)) {
            fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(outDir.toFile()));
            fm.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(outDir.toFile()));
            List<JavaFileObject> units = sources.entrySet().stream()
                    .map(e -> (JavaFileObject) new InMemoryJavaSource(e.getKey(), e.getValue()))
                    .toList();
            List<String> opts = List.of(
                    "--release", "25",
                    "-parameters",
                    "-classpath", System.getProperty("java.class.path")
            );
            JavaCompiler.CompilationTask task = compiler.getTask(null, fm, diagnostics, opts, null, units);
            task.setProcessors(List.of(new RestRepositoryProcessor()));
            boolean ok = Boolean.TRUE.equals(task.call());
            StringBuilder msgs = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                msgs.append(d.getKind()).append(": ").append(d.getMessage(null)).append("\n");
            }
            List<String> generated = new java.util.ArrayList<>();
            try (var walk = Files.walk(outDir)) {
                walk.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                    String rel = outDir.relativize(p).toString();
                    generated.add(rel.substring(0, rel.length() - 5).replace('/', '.'));
                });
            }
            return new CompilationResult(ok, msgs.toString(), generated);
        } finally {
            try (var walk = Files.walk(outDir)) {
                walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    private static final class InMemoryJavaSource extends SimpleJavaFileObject {
        private final String code;

        InMemoryJavaSource(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
