package io.jettra.server.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Text;
import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;
import io.jettra.server.jakarta.faces.FacesContext;
import io.jettra.server.jakarta.websocket.WebSocketSession;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pruebas de integración del motor UI JettraFlux en JettraServer.
 * 100% probado bajo el framework JettraTest.
 */
public class FluxServerIntegrationTest {

    public static class TestDashboardView extends FluxView {
        @Override
        public Widget build() {
            return Div.of(
                Text.of("Bienvenido al Dashboard de Control"),
                Button.of("Guardar Cambios")
            );
        }
    }

    @Test
    @DisplayName("Verificar renderizado HTML5 del árbol de componentes con FluxRenderer")
    public void testFluxRenderer() {
        FluxRenderer renderer = new FluxRenderer();
        Div root = Div.of(
            Text.of("Texto de Prueba Reactivo"),
            Button.of("Pulsar Aquí")
        );

        String html = renderer.render(root, "Página de Prueba");

        JettraAssert.assertNotNull(html, "El HTML generado no puede ser nulo");
        JettraAssert.assertTrue(html.contains("<!DOCTYPE html>"), "Debe ser HTML5 válido");
        JettraAssert.assertTrue(html.contains("<title>Página de Prueba</title>"), "Debe contener el título");
        JettraAssert.assertTrue(html.contains("Texto de Prueba Reactivo"), "Debe contener el texto del widget");
        JettraAssert.assertTrue(html.contains("Pulsar Aquí"), "Debe contener el botón del widget");
    }

    @Test
    @DisplayName("Verificar despacho HTTP de página JettraFlux sobre JettraServer")
    public void testFluxPageHttpEndpoint() throws Exception {
        ServerConfig config = new ServerConfig().setPort(9210).setStorageMode(StorageMode.JVM_RAM);
        JettraServer server = JettraServer.create(config);

        FluxModule flux = FluxModule.attach(server);
        flux.page("/app/dashboard", "Dashboard Principal", TestDashboardView.class);

        server.start();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9210/app/dashboard"))
            .GET()
            .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        JettraAssert.assertEquals(200, res.statusCode(), "Status HTTP debe ser 200");
        JettraAssert.assertTrue(res.headers().firstValue("Content-Type").orElse("").contains("text/html"), "Debe ser Content-Type text/html");
        JettraAssert.assertTrue(res.body().contains("Bienvenido al Dashboard de Control"), "Debe contener el contenido de la vista");
        JettraAssert.assertTrue(res.body().contains("Guardar Cambios"), "Debe contener el botón");

        server.stop();
    }

    @Test
    @DisplayName("Verificar difusión de parches DOM con FluxWebSocketBridge")
    public void testFluxWebSocketBridge() throws Exception {
        FluxWebSocketBridge bridge = new FluxWebSocketBridge();
        AtomicReference<String> receivedMessage = new AtomicReference<>();

        WebSocketSession mockSession = new WebSocketSession("session-101", receivedMessage::set);
        bridge.registerSession(mockSession);
        JettraAssert.assertEquals(1L, bridge.getActiveSessionCount(), "Debe registrar 1 sesión activa");

        bridge.broadcastPatch("counter-val", "<span>99</span>");

        JettraAssert.assertNotNull(receivedMessage.get(), "Debe recibir el mensaje serializado");
        JettraAssert.assertTrue(receivedMessage.get().contains("\"action\":\"PATCH\""), "Debe ser una acción PATCH");
        JettraAssert.assertTrue(receivedMessage.get().contains("\"target\":\"counter-val\""), "Debe apuntar a counter-val");
        JettraAssert.assertTrue(receivedMessage.get().contains("<span>99</span>"), "Debe incluir el HTML");

        bridge.unregisterSession(mockSession);
        JettraAssert.assertEquals(0L, bridge.getActiveSessionCount(), "Debe quedar sin sesiones activas");
    }

    @Test
    @DisplayName("Verificar Swagger-UI interactivo con Acreditación generado por JettraFlux")
    public void testSwaggerFluxEndpoint() throws Exception {
        ServerConfig config = new ServerConfig().setPort(9211).setStorageMode(StorageMode.JVM_RAM);
        JettraServer server = JettraServer.create(config);

        FluxModule flux = FluxModule.attach(server);
        flux.swagger("/swagger-ui", java.util.List.of());

        server.start();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9211/swagger-ui"))
            .GET()
            .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        JettraAssert.assertEquals(200, res.statusCode(), "Status HTTP de Swagger debe ser 200");
        JettraAssert.assertTrue(res.body().contains("JettraServer API") || res.body().contains("OpenAPI"), "Debe contener el título OpenAPI");
        JettraAssert.assertTrue(res.body().contains("Panel de Acreditación de Usuario"), "Debe contener el panel de acreditación");
        JettraAssert.assertTrue(res.body().contains("jettraLogin"), "Debe contener la función JS de acreditación");

        server.stop();
    }

    @Test
    @DisplayName("Verificar puente Jakarta Faces a JettraFlux")
    public void testFluxFacesBridge() {
        FacesContext context = new FacesContext("/views/home.xhtml");
        context.getViewMap().put("username", "avbravo");
        context.getViewMap().put("role", "ADMIN");

        Widget widget = FluxFacesBridge.fromFacesContext(context);
        JettraAssert.assertNotNull(widget, "El widget resultante no debe ser nulo");

        FluxRenderer renderer = new FluxRenderer();
        String html = renderer.render(widget, "Faces Interop");

        JettraAssert.assertTrue(html.contains("/views/home.xhtml"), "Debe incluir el viewId");
        JettraAssert.assertTrue(html.contains("username = avbravo"), "Debe incluir los atributos de la vista");
        JettraAssert.assertTrue(html.contains("role = ADMIN"), "Debe incluir el rol");
    }
}
