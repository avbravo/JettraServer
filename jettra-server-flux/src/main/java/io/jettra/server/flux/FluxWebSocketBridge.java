package io.jettra.server.flux;

import io.jettra.server.jakarta.websocket.WebSocketSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Puente reactivo para actualización de componentes JettraFlux en tiempo real mediante WebSockets.
 * Transmite parches incrementales del DOM y eventos de interacción.
 */
public class FluxWebSocketBridge {

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session) {
        activeSessions.put(session.getId(), session);
    }

    public void unregisterSession(WebSocketSession session) {
        activeSessions.remove(session.getId());
    }

    public void broadcastPatch(String targetElementId, String innerHtmlPatch) {
        String payload = String.format("{\"action\":\"PATCH\",\"target\":\"%s\",\"html\":\"%s\"}",
            escapeJson(targetElementId), escapeJson(innerHtmlPatch));

        for (WebSocketSession session : activeSessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendText(payload);
                } catch (IOException ignored) {}
            }
        }
    }

    public void sendPatch(WebSocketSession session, String targetElementId, String innerHtmlPatch) throws IOException {
        if (session != null && session.isOpen()) {
            String payload = String.format("{\"action\":\"PATCH\",\"target\":\"%s\",\"html\":\"%s\"}",
                escapeJson(targetElementId), escapeJson(innerHtmlPatch));
            session.sendText(payload);
        }
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
