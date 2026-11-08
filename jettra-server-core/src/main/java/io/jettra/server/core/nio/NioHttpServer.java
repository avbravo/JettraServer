package io.jettra.server.core.nio;

import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.http.HttpFilter;
import io.jettra.server.core.http.HttpFilterChain;
import io.jettra.server.core.http.HttpMethod;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import io.jettra.server.core.http.HttpRouter;
import io.jettra.server.core.http.HttpStatus;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Servidor HTTP Asíncrono de Ultra-Alto Rendimiento basado en Java NIO y Project Loom (Virtual Threads).
 * Ofrece arranques en frío inferiores a 100 ms y manejo masivo de conexiones concurrentes sin bloqueo de hilos de SO.
 */
public class NioHttpServer {

    private final ServerConfig config;
    private final HttpRouter router;
    private final List<HttpFilter> filters = new CopyOnWriteArrayList<>();
    private ServerSocketChannel serverChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private Thread acceptorThread;

    public NioHttpServer(ServerConfig config, HttpRouter router) {
        this.config = config;
        this.router = router;
    }

    public void addFilter(HttpFilter filter) {
        if (filter != null) filters.add(filter);
    }

    public synchronized void start() throws IOException {
        if (running.get()) return;

        serverChannel = ServerSocketChannel.open();
        try {
            serverChannel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
        } catch (Exception ignored) {}
        try {
            serverChannel.setOption(java.net.StandardSocketOptions.SO_REUSEPORT, true);
        } catch (Exception ignored) {}
        serverChannel.configureBlocking(true); // El bloqueo en Virtual Threads no penaliza el SO
        serverChannel.bind(new InetSocketAddress(config.getHost(), config.getPort()), config.getBacklog());
        running.set(true);

        // Hilo virtual aceptador de conexiones entrantes
        acceptorThread = Thread.ofVirtual().name("jettra-acceptor").start(this::acceptLoop);
    }

    public synchronized void stop() {
        if (!running.get()) return;
        running.set(false);
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } catch (IOException ignored) {}
        shutdownLatch.countDown();
    }

    public void await() throws InterruptedException {
        shutdownLatch.await();
    }

    public boolean isRunning() {
        return running.get() && serverChannel != null && serverChannel.isOpen();
    }

    public int getPort() {
        if (serverChannel != null && serverChannel.isOpen()) {
            try {
                return ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
            } catch (IOException ignored) {}
        }
        return config.getPort();
    }

    private void acceptLoop() {
        while (running.get() && serverChannel != null && serverChannel.isOpen()) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    clientChannel.configureBlocking(true);
                    // Despachar el socket a un Hilo Virtual dedicado
                    Thread.ofVirtual().name("jettra-worker-", 0).start(() -> handleConnection(clientChannel));
                }
            } catch (AsynchronousCloseException e) {
                break; // Servidor cerrado
            } catch (IOException e) {
                if (!running.get()) break;
            }
        }
    }

    private void handleConnection(SocketChannel channel) {
        try (channel) {
            channel.socket().setTcpNoDelay(true);
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            ByteArrayOutputStream rawRequestBytes = new ByteArrayOutputStream();

            int bytesRead;
            int headerEndIndex = -1;

            while ((bytesRead = channel.read(buffer)) > 0) {
                buffer.flip();
                byte[] chunk = new byte[buffer.remaining()];
                buffer.get(chunk);
                rawRequestBytes.write(chunk);
                buffer.clear();

                byte[] allBytes = rawRequestBytes.toByteArray();
                headerEndIndex = findHeaderEnd(allBytes);
                if (headerEndIndex >= 0) {
                    break;
                }
            }

            if (rawRequestBytes.size() == 0 || headerEndIndex < 0) {
                return;
            }

            byte[] fullData = rawRequestBytes.toByteArray();
            String headerText = new String(fullData, 0, headerEndIndex, StandardCharsets.US_ASCII);
            String[] lines = headerText.split("\r\n");
            if (lines.length == 0) return;

            // Línea de solicitud: METHOD PATH HTTP/1.1
            String[] requestLine = lines[0].split(" ");
            if (requestLine.length < 2) return;

            HttpMethod method = HttpMethod.fromString(requestLine[0]);
            String rawUri = requestLine[1];

            // Cabeceras (Case-Insensitive para soportar estándares HTTP y navegadores)
            Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (int i = 1; i < lines.length; i++) {
                int colon = lines[i].indexOf(':');
                if (colon > 0) {
                    String name = lines[i].substring(0, colon).trim();
                    String value = lines[i].substring(colon + 1).trim();
                    headers.put(name, value);
                }
            }

            // Cuerpo
            int bodyStartIndex = headerEndIndex + 4;
            int contentLength = 0;
            String clHeader = headers.get("Content-Length");
            if (clHeader != null) {
                try {
                    contentLength = Integer.parseInt(clHeader);
                } catch (NumberFormatException ignored) {}
            }

            ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
            int alreadyReadBody = fullData.length - bodyStartIndex;
            if (alreadyReadBody > 0) {
                bodyBytes.write(fullData, bodyStartIndex, Math.min(alreadyReadBody, contentLength));
            }

            while (bodyBytes.size() < contentLength) {
                buffer.clear();
                int read = channel.read(buffer);
                if (read <= 0) break;
                buffer.flip();
                byte[] chunk = new byte[buffer.remaining()];
                buffer.get(chunk);
                bodyBytes.write(chunk);
            }

            HttpRequest request = new HttpRequest(method, rawUri, headers, bodyBytes.toByteArray(), channel.getRemoteAddress());
            HttpResponse response = new HttpResponse();

            // Configurar CORS básico si está habilitado
            if (config.isCorsEnabled()) {
                response.header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS")
                        .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            }

            // Ejecutar cadena de filtros y enrutador
            try {
                if (method == HttpMethod.OPTIONS) {
                    response.status(HttpStatus.NO_CONTENT);
                } else {
                    HttpFilterChain chain = new HttpFilterChain(filters, router::dispatch);
                    chain.doFilter(request, response);
                }
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(Map.of("error", "Internal Server Error", "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error"));
            }

            // Enviar respuesta
            byte[] responseBytes = response.toHttp11Bytes();
            ByteBuffer writeBuffer = ByteBuffer.wrap(responseBytes);
            while (writeBuffer.hasRemaining()) {
                channel.write(writeBuffer);
            }
            response.markCommitted();

        } catch (IOException ignored) {
            // Conexión cerrada o error de red
        }
    }

    private static int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }
}
