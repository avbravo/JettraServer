package io.jettra.server.core;

import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Pruebas de integración del servidor NIO y Virtual Threads de JettraServer.
 * Utiliza 100% JettraTest.
 */
public class NioHttpServerTest {

    @Test
    @DisplayName("Verificar arranque en frío y petición HTTP GET /health con Virtual Threads")
    public void testServerStartupAndGetEndpoint() throws Exception {
        ServerConfig config = new ServerConfig()
            .setPort(9188)
            .setStorageMode(StorageMode.JVM_RAM);

        JettraServer server = JettraServer.create(config);

        server.get("/api/greet", (req, res) -> {
            res.json(Map.of("message", "Hola desde JettraServer Java 25", "thread", Thread.currentThread().toString()));
        });

        server.post("/api/echo", (req, res) -> {
            String body = req.getBodyAsString();
            res.body("Echo: " + body);
        });

        server.start();
        JettraAssert.assertTrue(server.isRunning(), "El servidor debe estar en ejecución");

        HttpClient client = HttpClient.newHttpClient();

        // Probar GET /api/greet
        HttpRequest getReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9188/api/greet"))
            .GET()
            .build();
        HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        JettraAssert.assertEquals(200, getRes.statusCode(), "Status HTTP debe ser 200");
        JettraAssert.assertTrue(getRes.body().contains("Hola desde JettraServer"), "Respuesta debe contener el saludo");

        // Probar POST /api/echo
        HttpRequest postReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9188/api/echo"))
            .POST(HttpRequest.BodyPublishers.ofString("Prueba Payload 123"))
            .build();
        HttpResponse<String> postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
        JettraAssert.assertEquals(200, postRes.statusCode(), "Status POST debe ser 200");
        JettraAssert.assertEquals("Echo: Prueba Payload 123", postRes.body(), "Cuerpo de eco debe coincidir");

        // Probar GET /health
        HttpRequest healthReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9188/health"))
            .GET()
            .build();
        HttpResponse<String> healthRes = client.send(healthReq, HttpResponse.BodyHandlers.ofString());
        JettraAssert.assertEquals(200, healthRes.statusCode(), "Status de salud debe ser 200");
        JettraAssert.assertTrue(healthRes.body().contains("UP"), "Salud debe reportar UP");

        // Probar GET /metrics
        HttpRequest metricsReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9188/metrics"))
            .GET()
            .build();
        HttpResponse<String> metricsRes = client.send(metricsReq, HttpResponse.BodyHandlers.ofString());
        JettraAssert.assertEquals(200, metricsRes.statusCode(), "Status de métricas debe ser 200");
        JettraAssert.assertTrue(metricsRes.body().contains("JVM_RAM"), "Métricas deben incluir el modo de persistencia");

        server.stop();
        JettraAssert.assertFalse(server.isRunning(), "El servidor debe detenerse");
    }
}
