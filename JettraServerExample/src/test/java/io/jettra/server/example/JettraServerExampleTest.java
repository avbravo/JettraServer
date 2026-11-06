package io.jettra.server.example;

import io.jettra.server.core.JettraServer;
import io.jettra.server.core.storage.StorageMode;
import io.jettra.server.example.model.Product;
import io.jettra.server.example.service.ProductService;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Suite de pruebas integradas de JettraServerExample validada con el framework JettraTest.
 * 100% libre de dependencias JUnit.
 */
public class JettraServerExampleTest {

    private static final int TEST_PORT = 9333;

    @Test
    @DisplayName("1. Verificar renderizado del Formulario de Login PrimeFaces")
    public void testLoginPageRendering() throws Exception {
        JettraServer server = App.startApplication(TEST_PORT, StorageMode.JVM_RAM);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/login.xhtml"))
                .GET()
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            JettraAssert.assertEquals(200, res.statusCode(), "El formulario de Login debe responder HTTP 200");
            JettraAssert.assertTrue(res.body().contains("JettraServer"), "Debe contener el título de la app");
            JettraAssert.assertTrue(res.body().contains("name=\"username\""), "Debe contener el campo de usuario");
            JettraAssert.assertTrue(res.body().contains("name=\"password\""), "Debe contener el campo de contraseña");
            JettraAssert.assertTrue(res.body().contains("Iniciar Sesión"), "Debe contener el botón de inicio de sesión");
        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("2. Verificar validación de credenciales en Login (Rechazo de credenciales inválidas)")
    public void testLoginValidationFailure() throws Exception {
        JettraServer server = App.startApplication(TEST_PORT + 1, StorageMode.JVM_RAM);
        try {
            HttpClient client = HttpClient.newHttpClient();
            String formData = "username=usuario_invalido&password=clave_erronea";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 1) + "/login.xhtml"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            JettraAssert.assertEquals(200, res.statusCode(), "Debe recargar el formulario con error");
            JettraAssert.assertTrue(res.body().contains("Credenciales inválidas"), "Debe mostrar el mensaje de validación de error");
        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("3. Verificar autenticación exitosa y redirección al Dashboard")
    public void testLoginSuccessRedirect() throws Exception {
        JettraServer server = App.startApplication(TEST_PORT + 2, StorageMode.JVM_RAM);
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
            String formData = "username=admin&password=admin123";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 2) + "/login.xhtml"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            JettraAssert.assertEquals(302, res.statusCode(), "Las credenciales válidas deben redirigir con HTTP 302");
            String location = res.headers().firstValue("Location").orElse("");
            JettraAssert.assertTrue(location.contains("/dashboard.xhtml"), "Debe redirigir al Dashboard");
            String setCookie = res.headers().firstValue("Set-Cookie").orElse("");
            JettraAssert.assertTrue(setCookie.contains("JSESSIONID="), "Debe emitir cookie de sesión JSESSIONID");
        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("4. Verificar estructura completa de la Plantilla Dashboard (Top, Left, Center, Footer)")
    public void testDashboardTemplateStructure() throws Exception {
        JettraServer server = App.startApplication(TEST_PORT + 3, StorageMode.JVM_RAM);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 3) + "/dashboard.xhtml"))
                .GET()
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            JettraAssert.assertEquals(200, res.statusCode(), "El dashboard debe responder HTTP 200");
            String html = res.body();

            // TOP: Header, Título, Modo Persistencia, Usuario y Logout
            JettraAssert.assertTrue(html.contains("ui-layout-top"), "Debe contener la sección TOP (Header)");
            JettraAssert.assertTrue(html.contains("Cerrar Sesión"), "Debe contener el botón de Logout en TOP");

            // LEFT: Menú lateral con enlaces a páginas de ejemplo y JettraFlux
            JettraAssert.assertTrue(html.contains("ui-layout-left"), "Debe contener la sección LEFT (Sidebar Menu)");
            JettraAssert.assertTrue(html.contains("/flux/storage-monitor"), "Debe incluir enlace a vista JettraFlux Storage");
            JettraAssert.assertTrue(html.contains("/flux/telemetry"), "Debe incluir enlace a vista JettraFlux Telemetry");
            JettraAssert.assertTrue(html.contains("/flux/counter"), "Debe incluir enlace a vista JettraFlux Counter");
            JettraAssert.assertTrue(html.contains("/productos.xhtml"), "Debe incluir enlace a página de Catálogo JPA");

            // CENTER: Tarjetas KPI, métricas y contenido principal
            JettraAssert.assertTrue(html.contains("ui-layout-center"), "Debe contener la sección CENTER (Contenido Principal)");
            JettraAssert.assertTrue(html.contains("Modo Persistencia"), "Debe mostrar tarjeta de modo de persistencia");
            JettraAssert.assertTrue(html.contains("Catálogo Productos (JPA)"), "Debe mostrar métricas de productos");
            JettraAssert.assertTrue(html.contains("Concurrencia Loom"), "Debe mostrar el indicador de Virtual Threads");

            // FOOTER: Barra inferior de estado del servidor
            JettraAssert.assertTrue(html.contains("ui-layout-footer"), "Debe contener la sección FOOTER (Pie de página)");
            JettraAssert.assertTrue(html.contains("OPERATIVO"), "Debe mostrar estado operativo en FOOTER");
        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("5. Verificar invocación y renderizado de Vistas Reactivas JettraFlux desde el menú")
    public void testJettraFluxViewsInvocation() throws Exception {
        JettraServer server = App.startApplication(TEST_PORT + 4, StorageMode.JVM_RAM);
        try {
            HttpClient client = HttpClient.newHttpClient();

            // 1. Storage Monitor JettraFlux
            HttpRequest reqStorage = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 4) + "/flux/storage-monitor"))
                .GET()
                .build();
            HttpResponse<String> resStorage = client.send(reqStorage, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, resStorage.statusCode(), "Vista JettraFlux Storage debe responder 200");
            JettraAssert.assertTrue(resStorage.body().contains("Monitor de Almacenamiento Reactivo"), "Debe contener el título del widget JettraFlux");
            JettraAssert.assertTrue(resStorage.body().contains("JVM_RAM"), "Debe reportar el modo activo");

            // 2. Telemetry JettraFlux
            HttpRequest reqTelemetry = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 4) + "/flux/telemetry"))
                .GET()
                .build();
            HttpResponse<String> resTelemetry = client.send(reqTelemetry, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, resTelemetry.statusCode(), "Vista JettraFlux Telemetry debe responder 200");
            JettraAssert.assertTrue(resTelemetry.body().contains("Telemetría en Tiempo Real"), "Debe renderizar la telemetría");

            // 3. Counter JettraFlux
            HttpRequest reqCounter = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 4) + "/flux/counter"))
                .GET()
                .build();
            HttpResponse<String> resCounter = client.send(reqCounter, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, resCounter.statusCode(), "Vista JettraFlux Counter debe responder 200");
            JettraAssert.assertTrue(resCounter.body().contains("Demostración Interactiva JettraFlux"), "Debe renderizar el contador");
        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("6. Verificar API RESTful de Productos y Server Info")
    public void testRestEndpoints() throws Exception {
        JettraServer server = App.startApplication(TEST_PORT + 5, StorageMode.JVM_RAM);
        try {
            HttpClient client = HttpClient.newHttpClient();

            // /api/productos
            HttpRequest reqProds = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 5) + "/api/productos"))
                .GET()
                .build();
            HttpResponse<String> resProds = client.send(reqProds, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, resProds.statusCode(), "Endpoint REST debe responder HTTP 200");
            JettraAssert.assertTrue(resProds.body().contains("PRD-01"), "Debe incluir productos del catálogo");

            // /api/info
            HttpRequest reqInfo = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 5) + "/api/info"))
                .GET()
                .build();
            HttpResponse<String> resInfo = client.send(reqInfo, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, resInfo.statusCode(), "Endpoint Info debe responder HTTP 200");
            JettraAssert.assertTrue(resInfo.body().contains("JettraServerExample"), "Debe reportar el nombre de la aplicación");
        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("7. Verificar vista reactiva CatalogoFlux (JettraFlux) y renderizado I18N")
    public void testCatalogoFluxViewAndI18n() throws Exception {
        JettraServer server = App.startApplication(TEST_PORT + 6, StorageMode.JVM_RAM);
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Petición a /flux/catalogo
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + (TEST_PORT + 6) + "/flux/catalogo"))
                .GET()
                .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            JettraAssert.assertEquals(200, res.statusCode(), "CatalogoFlux debe responder HTTP 200");
            String body = res.body();

            // Verificar renderizado en la sección central del Dashboard
            JettraAssert.assertTrue(body.contains("ui-layout-center"), "Debe estar integrado en la sección CENTER del layout");
            JettraAssert.assertTrue(body.contains("Catálogo Reactivo de Productos"), "Debe contener el título desde messages.properties");
            JettraAssert.assertTrue(body.contains("Java 25 Reactive DSL"), "Debe contener el badge reactivo DSL");
            JettraAssert.assertTrue(body.contains("PRD-01"), "Debe contener los productos de la base de datos");
            JettraAssert.assertTrue(body.contains("Registrar Producto"), "Debe contener el botón de guardado I18N");

            // Verificar clase utilitaria Messages
            String appTitle = io.jettra.server.example.util.Messages.get("app.title");
            JettraAssert.assertEquals("JettraServer Enterprise", appTitle, "El título de la aplicación debe coincidir con messages.properties");
            String catalogTitle = io.jettra.server.example.util.Messages.get("flux.catalog.title");
            JettraAssert.assertEquals("Catálogo Reactivo de Productos", catalogTitle, "La etiqueta debe provenir de messages.properties");
        } finally {
            server.stop();
        }
    }
}

