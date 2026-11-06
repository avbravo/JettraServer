package io.jettra.server.core.http;

import io.jettra.json.JettraJson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación fluida de la respuesta HTTP enviada al cliente.
 */
public class HttpResponse {

    private HttpStatus status = HttpStatus.OK;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];
    private boolean committed = false;

    public HttpResponse() {
        headers.put("Server", "JettraServer/1.0.0 (Java 25; Loom; Slotted-Pages)");
        headers.put("Connection", "keep-alive");
    }

    public HttpResponse status(HttpStatus status) {
        if (committed) throw new IllegalStateException("Respuesta ya confirmada");
        this.status = status;
        return this;
    }

    public HttpResponse status(int code) {
        return status(HttpStatus.fromCode(code));
    }

    public HttpStatus getStatus() {
        return status;
    }

    public HttpResponse header(String name, String value) {
        if (committed) throw new IllegalStateException("Respuesta ya confirmada");
        headers.put(name, value);
        return this;
    }

    public HttpResponse contentType(String mimeType) {
        return header("Content-Type", mimeType);
    }

    public HttpResponse body(byte[] body) {
        if (committed) throw new IllegalStateException("Respuesta ya confirmada");
        this.body = body != null ? body : new byte[0];
        header("Content-Length", String.valueOf(this.body.length));
        return this;
    }

    public HttpResponse body(String text) {
        if (headers.get("Content-Type") == null) {
            contentType("text/plain; charset=UTF-8");
        }
        return body(text != null ? text.getBytes(StandardCharsets.UTF_8) : new byte[0]);
    }

    private static final JettraJson JSON_SERIALIZER = new JettraJson();

    public HttpResponse json(Object object) {
        contentType("application/json; charset=UTF-8");
        String jsonStr = JSON_SERIALIZER.toJson(object);
        return body(jsonStr.getBytes(StandardCharsets.UTF_8));
    }

    public HttpResponse html(String html) {
        contentType("text/html; charset=UTF-8");
        return body(html != null ? html.getBytes(StandardCharsets.UTF_8) : new byte[0]);
    }

    public byte[] getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public boolean isCommitted() {
        return committed;
    }

    public void markCommitted() {
        this.committed = true;
    }

    /**
     * Construye el arreglo binario de bytes HTTP/1.1 para transmisión directa por socket NIO.
     */
    public byte[] toHttp11Bytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(headers.size() * 64 + body.length + 64);
        try {
            // Status Line
            String statusLine = "HTTP/1.1 " + status.getCode() + " " + status.getReasonPhrase() + "\r\n";
            baos.write(statusLine.getBytes(StandardCharsets.US_ASCII));

            // Encabezados
            if (!headers.containsKey("Content-Length")) {
                headers.put("Content-Length", String.valueOf(body.length));
            }
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String h = entry.getKey() + ": " + entry.getValue() + "\r\n";
                baos.write(h.getBytes(StandardCharsets.US_ASCII));
            }

            // Separador de cabeceras y cuerpo
            baos.write("\r\n".getBytes(StandardCharsets.US_ASCII));

            // Cuerpo
            if (body.length > 0) {
                baos.write(body);
            }
        } catch (IOException ignored) {}

        return baos.toByteArray();
    }
}
