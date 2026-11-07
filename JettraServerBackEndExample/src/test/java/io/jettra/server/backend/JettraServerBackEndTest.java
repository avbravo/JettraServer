package io.jettra.server.backend;

import io.jettra.json.JettraJson;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;

import java.io.File;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Suite integral de pruebas para los microservicios de JettraServerBackEndExample.
 * Valida Acreditación JWT, Swagger-UI con JettraFlux y endpoints REST bajo JettraTest.
 */
public class JettraServerBackEndTest {

    private static final JettraJson JSON = new JettraJson();

    private int findFreePort() {
        try (ServerSocket ss = new ServerSocket(0)) {
            ss.setReuseAddress(true);
            return ss.getLocalPort();
        } catch (Exception e) {
            return 9450;
        }
    }

    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteDir(f);
                    else f.delete();
                }
            }
            dir.delete();
        }
    }

    @Test
    @DisplayName("Verificar endpoint público de Ping / Telemetría")
    public void testPingEndpoint() throws Exception {
        int port = findFreePort();
        Path tempDir = Files.createTempDirectory("backend-test-ping");
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("data.dir", tempDir.toString());

        App app = new App();
        try {
            app.startApplication(new String[0]);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/api/v1/system/ping"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, res.statusCode(), "Status HTTP debe ser 200");
            JettraAssert.assertTrue(res.body().contains("\"status\":\"UP\""), "Debe indicar status UP");
        } finally {
            app.stop();
            deleteDir(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Verificar que microservicio protegido rechaza petición sin Token JWT (401)")
    public void testProtectedEndpointWithoutAuthFailsWith401() throws Exception {
        int port = findFreePort();
        Path tempDir = Files.createTempDirectory("backend-test-sec401");
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("data.dir", tempDir.toString());

        App app = new App();
        try {
            app.startApplication(new String[0]);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/api/v1/customers"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(401, res.statusCode(), "Status HTTP debe ser 401 Unauthorized");
            JettraAssert.assertTrue(res.body().contains("Acreditación requerida"), "Debe indicar acreditación requerida");
        } finally {
            app.stop();
            deleteDir(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Autenticación completa con JettraSecurityDB y acceso con Token Bearer JWT")
    public void testAuthenticationAndProtectedEndpointAccess() throws Exception {
        int port = findFreePort();
        Path tempDir = Files.createTempDirectory("backend-test-auth");
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("data.dir", tempDir.toString());

        App app = new App();
        try {
            app.startApplication(new String[0]);
            HttpClient client = HttpClient.newHttpClient();

            // 1. Iniciar sesión y obtener token JWT para superusuario admin
            String loginJson = "{\"username\":\"admin\",\"password\":\"admin\"}";
            HttpRequest loginReq = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                    .build();

            HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, loginRes.statusCode(), "Autenticación debe responder 200 OK");

            Map<?, ?> authData = JSON.fromJson(loginRes.body(), Map.class);
            String rawToken = (String) authData.get("rawToken");
            JettraAssert.assertNotNull(rawToken, "Debe retornar el rawToken");
            JettraAssert.assertFalse(rawToken.isBlank(), "El token no puede estar vacío");

            // 2. Consumir microservicio protegido con el token obtenido
            HttpRequest custReq = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/api/v1/customers"))
                    .header("Authorization", "Bearer " + rawToken)
                    .GET()
                    .build();

            HttpResponse<String> custRes = client.send(custReq, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, custRes.statusCode(), "Microservicio debe responder 200 OK con token válido");
            JettraAssert.assertTrue(custRes.body().contains("Acme Corporation"), "Debe retornar clientes");

            // 3. Crear nuevo cliente mediante POST protegido
            String newCustJson = "{\"name\":\"TechCorp Solutions\",\"email\":\"info@techcorp.io\",\"company\":\"TechCorp\",\"tier\":\"ENTERPRISE\",\"active\":true}";
            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/api/v1/customers"))
                    .header("Authorization", "Bearer " + rawToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(newCustJson))
                    .build();

            HttpResponse<String> createRes = client.send(createReq, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(201, createRes.statusCode(), "Debe retornar 201 Created");
            JettraAssert.assertTrue(createRes.body().contains("TechCorp Solutions"), "Debe incluir cliente creado");
        } finally {
            app.stop();
            deleteDir(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Verificar interfaz Swagger-UI generada por JettraFlux con panel de Acreditación")
    public void testSwaggerUiFluxEndpoint() throws Exception {
        int port = findFreePort();
        Path tempDir = Files.createTempDirectory("backend-test-swagger");
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("data.dir", tempDir.toString());

        App app = new App();
        try {
            app.startApplication(new String[0]);
            HttpClient client = HttpClient.newHttpClient();

            // Consultar Swagger-UI en /swagger-ui
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/swagger-ui"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, res.statusCode(), "Swagger-UI debe responder 200 OK");
            JettraAssert.assertTrue(res.headers().firstValue("Content-Type").orElse("").contains("text/html"), "Debe retornar HTML");
            JettraAssert.assertTrue(res.body().contains("Panel de Acreditación de Usuario"), "Debe incluir el panel de acreditación");
            JettraAssert.assertTrue(res.body().contains("jettraLogin"), "Debe contener la función JS de autenticación");
            JettraAssert.assertTrue(res.body().contains("/api/v1/customers"), "Debe documentar ruta de clientes");
            JettraAssert.assertTrue(res.body().contains("/api/v1/orders"), "Debe documentar ruta de órdenes");
        } finally {
            app.stop();
            deleteDir(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Verificar especificación OpenAPI 3.0.0 JSON con esquemas de seguridad")
    public void testOpenApiJsonSpecification() throws Exception {
        int port = findFreePort();
        Path tempDir = Files.createTempDirectory("backend-test-openapi");
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("data.dir", tempDir.toString());

        App app = new App();
        try {
            app.startApplication(new String[0]);
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/openapi.json"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, res.statusCode(), "OpenAPI JSON debe responder 200 OK");
            JettraAssert.assertTrue(res.body().contains("\"openapi\":\"3.0.0\""), "Debe ser versión OpenAPI 3.0.0");
            JettraAssert.assertTrue(res.body().contains("bearerAuth"), "Debe incluir esquema de seguridad bearerAuth");
            JettraAssert.assertTrue(res.body().contains("/api/v1/customers"), "Debe contener path de clientes");
            JettraAssert.assertTrue(res.body().contains("/api/v1/orders"), "Debe contener path de órdenes");
        } finally {
            app.stop();
            deleteDir(tempDir.toFile());
        }
    }
}

