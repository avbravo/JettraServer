package io.jettra.server.jakarta;

import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;
import io.jettra.server.jakarta.cdi.CdiAnnotations;
import io.jettra.server.jakarta.cdi.CdiContainer;
import io.jettra.server.jakarta.concurrency.ManagedExecutorService;
import io.jettra.server.jakarta.jms.JmsBroker;
import io.jettra.server.jakarta.json.Jsonb;
import io.jettra.server.jakarta.persistence.EntityManager;
import io.jettra.server.jakarta.persistence.EntityManagerFactory;
import io.jettra.server.jakarta.persistence.PersistenceAnnotations;
import io.jettra.server.jakarta.rest.RestAnnotations;
import io.jettra.server.jakarta.servlet.HttpServlet;
import io.jettra.server.jakarta.servlet.HttpServletRequest;
import io.jettra.server.jakarta.servlet.HttpServletResponse;
import io.jettra.server.jakarta.transaction.JettraTransactionManager;
import io.jettra.server.jakarta.validation.ValidationEngine;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Suite integral de pruebas para todas las especificaciones Jakarta EE implementadas en JettraServer.
 * 100% probado bajo el framework JettraTest.
 */
public class JakartaSuiteTest {

    // -------------------------------------------------------------
    // Entidad de prueba JPA
    // -------------------------------------------------------------
    @PersistenceAnnotations.Entity
    public static class Producto implements Serializable {
        @PersistenceAnnotations.Id
        @PersistenceAnnotations.GeneratedValue
        private Long id;

        @PersistenceAnnotations.Column
        private String nombre;

        private double precio;

        public Producto() {}
        public Producto(String nombre, double precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }
    }

    // -------------------------------------------------------------
    // Beans CDI de prueba
    // -------------------------------------------------------------
    public static class AuditService {
        private boolean initialized = false;

        @CdiAnnotations.PostConstruct
        public void init() {
            this.initialized = true;
        }

        public boolean isInitialized() { return initialized; }
    }

    public static class OrderService {
        @CdiAnnotations.Inject
        private AuditService auditService;

        public AuditService getAuditService() { return auditService; }
    }

    public static class StandardCdiBean {
        private boolean initialized = false;

        @jakarta.annotation.PostConstruct
        public void init() {
            this.initialized = true;
        }

        public boolean isInitialized() { return initialized; }
    }

    public static class StandardConsumerBean {
        @jakarta.inject.Inject
        private StandardCdiBean standardBean;

        public StandardCdiBean getStandardBean() { return standardBean; }
    }

    // -------------------------------------------------------------
    // Recurso REST JAX-RS de prueba
    // -------------------------------------------------------------
    @RestAnnotations.Path("/api/v1/productos")
    public static class ProductoResource {

        @RestAnnotations.GET
        public Map<String, Object> list() {
            return Map.of("count", 2, "status", "OK");
        }

        @RestAnnotations.GET
        @RestAnnotations.Path("/{id}")
        public Map<String, Object> getById(@RestAnnotations.PathParam("id") long id) {
            return Map.of("id", id, "nombre", "Item-" + id);
        }

        @RestAnnotations.POST
        public Map<String, Object> create(Map<String, Object> payload) {
            return Map.of("created", true, "received", payload.getOrDefault("nombre", "unknown"));
        }
    }

    // -------------------------------------------------------------
    // Servlet Jakarta de prueba
    // -------------------------------------------------------------
    public static class SimpleHelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            resp.setContentType("text/plain");
            resp.getWriter().print("Hello from HttpServlet 6.1");
        }
    }

    // -------------------------------------------------------------
    // DTO de Validación
    // -------------------------------------------------------------
    public static class UserDto {
        @ValidationEngine.NotNull
        @ValidationEngine.Size(min = 3, max = 20)
        private String username;

        @ValidationEngine.Email
        private String email;

        @ValidationEngine.Min(18)
        private int age;

        public UserDto(String username, String email, int age) {
            this.username = username;
            this.email = email;
            this.age = age;
        }
    }

    @Test
    @DisplayName("Verificar Jakarta JPA 3.2 integrado con JettraStorageEngine (JVM_RAM)")
    public void testJpaPersistenceEngine() {
        ServerConfig config = new ServerConfig().setStorageMode(StorageMode.JVM_RAM);
        JettraServer server = JettraServer.create(config);
        JakartaContainer container = JakartaContainer.attach(server);

        EntityManagerFactory emf = container.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        Producto p1 = new Producto("Laptop ThinkPad", 1299.99);
        Producto p2 = new Producto("Monitor 4K", 450.00);

        em.getTransaction().begin();
        em.persist(p1);
        em.persist(p2);
        em.getTransaction().commit();

        JettraAssert.assertNotNull(p1.getId(), "El ID debe ser autogenerado");
        JettraAssert.assertNotNull(p2.getId(), "El ID debe ser autogenerado");

        Producto found = em.find(Producto.class, p1.getId());
        JettraAssert.assertNotNull(found, "Debe encontrar el producto persistido");
        JettraAssert.assertEquals("Laptop ThinkPad", found.getNombre(), "El nombre debe coincidir");

        List<Producto> all = em.findAll(Producto.class);
        JettraAssert.assertEquals(2L, all.size(), "Debe recuperar todos los productos");

        em.remove(p1);
        JettraAssert.assertNull(em.find(Producto.class, p1.getId()), "El producto eliminado debe ser nulo");

        em.close();
    }

    @Test
    @DisplayName("Verificar Jakarta CDI 4.1 inyección e inicialización del ciclo de vida")
    public void testCdiContainerLifecycle() {
        CdiContainer cdi = new CdiContainer();
        OrderService orderService = cdi.getBean(OrderService.class);

        JettraAssert.assertNotNull(orderService, "OrderService debe crearse");
        JettraAssert.assertNotNull(orderService.getAuditService(), "AuditService debe inyectarse vía @Inject");
        JettraAssert.assertTrue(orderService.getAuditService().isInitialized(), "@PostConstruct debió ejecutarse");

        cdi.shutdown();
    }

    @Test
    @DisplayName("Verificar Jakarta REST JAX-RS 3.1 sobre HTTP NIO Virtual Threads")
    public void testJaxRsResourceInvocation() throws Exception {
        ServerConfig config = new ServerConfig().setPort(9199).setStorageMode(StorageMode.JVM_RAM);
        JettraServer server = JettraServer.create(config);
        JakartaContainer container = JakartaContainer.attach(server);

        container.getRestDispatcher().registerResource(ProductoResource.class);
        server.start();

        HttpClient client = HttpClient.newHttpClient();

        // 1. GET /api/v1/productos
        HttpRequest getReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9199/api/v1/productos"))
            .GET()
            .build();
        HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        JettraAssert.assertEquals(200, getRes.statusCode(), "GET list debe retornar 200");
        JettraAssert.assertTrue(getRes.body().contains("\"count\":2"), "Debe contener count 2");

        // 2. GET /api/v1/productos/42
        HttpRequest idReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9199/api/v1/productos/42"))
            .GET()
            .build();
        HttpResponse<String> idRes = client.send(idReq, HttpResponse.BodyHandlers.ofString());
        JettraAssert.assertEquals(200, idRes.statusCode(), "GET by id debe retornar 200");
        JettraAssert.assertTrue(idRes.body().contains("Item-42"), "Debe resolver @PathParam id");

        server.stop();
    }

    @Test
    @DisplayName("Verificar Jakarta Servlet 6.1 HttpServletBridge")
    public void testServletBridge() throws Exception {
        ServerConfig config = new ServerConfig().setPort(9200).setStorageMode(StorageMode.JVM_RAM);
        JettraServer server = JettraServer.create(config);
        JakartaContainer container = JakartaContainer.attach(server);

        container.getServletBridge().registerServlet("/servlet/hello", new SimpleHelloServlet());
        server.start();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9200/servlet/hello"))
            .GET()
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        JettraAssert.assertEquals(200, res.statusCode(), "Servlet debe responder 200");
        JettraAssert.assertEquals("Hello from HttpServlet 6.1", res.body(), "Cuerpo del servlet correcto");

        server.stop();
    }

    @Test
    @DisplayName("Verificar Jakarta Messaging JMS 3.1 broker en memoria")
    public void testJmsMessaging() throws Exception {
        JmsBroker broker = new JmsBroker();
        JmsBroker.Queue queue = broker.createQueue("orders.queue");

        broker.send(queue, new JmsBroker.JmsTextMessage("Nueva Orden #998", Map.of("priority", "HIGH")));
        JettraAssert.assertEquals(1L, broker.queueSize(queue), "La cola debe tener 1 mensaje");

        JmsBroker.JmsTextMessage received = broker.receive(queue, 100);
        JettraAssert.assertNotNull(received, "Debe recibir el mensaje");
        JettraAssert.assertEquals("Nueva Orden #998", received.text(), "Texto del mensaje JMS correcto");
        JettraAssert.assertEquals(0L, broker.queueSize(queue), "La cola debe quedar vacía");
    }

    @Test
    @DisplayName("Verificar Jakarta Concurrency 3.0 con Virtual Threads")
    public void testManagedExecutorService() throws Exception {
        try (ManagedExecutorService executor = new ManagedExecutorService()) {
            Future<String> future = executor.submit(() -> {
                JettraAssert.assertTrue(Thread.currentThread().isVirtual(), "Debe ejecutarse en un Virtual Thread");
                return "SUCCESS_LOOM";
            });
            String result = future.get();
            JettraAssert.assertEquals("SUCCESS_LOOM", result, "Resultado del executor debe coincidir");
        }
    }

    @Test
    @DisplayName("Verificar Jakarta Bean Validation 3.1")
    public void testBeanValidation() {
        // Objeto válido
        UserDto validUser = new UserDto("carlos", "carlos@example.com", 25);
        Set<ValidationEngine.ConstraintViolation> violations = ValidationEngine.validate(validUser);
        JettraAssert.assertTrue(violations.isEmpty(), "No debe tener violaciones");

        // Objeto con fallos: nombre corto, correo inválido, menor de edad
        UserDto invalidUser = new UserDto("a", "correo_invalido", 15);
        Set<ValidationEngine.ConstraintViolation> badViolations = ValidationEngine.validate(invalidUser);
        JettraAssert.assertEquals(3L, badViolations.size(), "Debe contener 3 violaciones de validación");
    }

    @Test
    @DisplayName("Verificar Jakarta Transactions JTA 2.0")
    public void testTransactionManager() {
        JettraTransactionManager tm = new JettraTransactionManager();
        JettraAssert.assertEquals(JettraTransactionManager.STATUS_NO_TRANSACTION, tm.getStatus(), "Estado inicial sin transacción");

        tm.begin();
        JettraAssert.assertEquals(JettraTransactionManager.STATUS_ACTIVE, tm.getStatus(), "Estado activo tras begin");

        tm.commit();
        JettraAssert.assertEquals(JettraTransactionManager.STATUS_COMMITTED, tm.getStatus(), "Estado confirmado tras commit");
    }

    @Test
    @DisplayName("Validar CDI con anotaciones oficiales jakarta.inject y jakarta.annotation")
    public void testOfficialJakartaCdi() {
        CdiContainer container = new CdiContainer();
        StandardConsumerBean consumer = container.getBean(StandardConsumerBean.class);

        JettraAssert.assertNotNull(consumer, "El consumidor CDI debe existir");
        JettraAssert.assertNotNull(consumer.getStandardBean(), "El bean inyectado con jakarta.inject.Inject debe existir");
        JettraAssert.assertTrue(consumer.getStandardBean().isInitialized(), "@PostConstruct de jakarta.annotation debe haberse ejecutado");
    }

    @Test
    @DisplayName("Verificar compatibilidad con Eclipse MicroProfile Config API")
    public void testMicroProfileConfigApi() {
        org.eclipse.microprofile.config.Config config = org.eclipse.microprofile.config.ConfigProvider.getConfig();
        JettraAssert.assertNotNull(config, "ConfigProvider debe resolver una instancia de Config");
    }
}
