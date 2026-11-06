package io.jettra.server.jakarta.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Representa una sesión WebSocket activa de Jakarta WebSocket 2.1.
 */
public class WebSocketSession implements Closeable {

    private final String id;
    private final Map<String, Object> userProperties = new ConcurrentHashMap<>();
    private final Consumer<String> textSender;
    private boolean open = true;

    public WebSocketSession(String id, Consumer<String> textSender) {
        this.id = id;
        this.textSender = textSender;
    }

    public String getId() { return id; }

    public Map<String, Object> getUserProperties() { return userProperties; }

    public void sendText(String message) throws IOException {
        if (!open) throw new IOException("Sesión WebSocket cerrada");
        if (textSender != null) {
            textSender.accept(message);
        }
    }

    public boolean isOpen() { return open; }

    @Override
    public void close() {
        open = false;
    }
}
