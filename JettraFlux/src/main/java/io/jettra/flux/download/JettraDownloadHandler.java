package io.jettra.flux.download;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Enterprise HTTP file streaming handler in Java 25+.
 * Leverages Virtual Threads (Thread.ofVirtual()) to stream resources off-thread
 * without blocking UI thread pools or reactive dispatchers.
 */
public final class JettraDownloadHandler {

    private static final int BUFFER_SIZE = 8192;

    private JettraDownloadHandler() {}

    /**
     * Streams a DownloadResource to an active HttpExchange using a Java 25 Virtual Thread.
     * Blocks the calling virtual thread until streaming completes while leaving carrier threads free.
     *
     * @param exchange active HTTP exchange
     * @param resource downloadable resource descriptor
     * @throws IOException if writing or network transfer fails
     */
    public static void sendDownload(HttpExchange exchange, DownloadResource resource) throws IOException {
        Objects.requireNonNull(exchange, "HttpExchange must not be null");
        Objects.requireNonNull(resource, "DownloadResource must not be null");

        CompletableFuture<Void> future = sendDownloadAsync(exchange, resource);
        try {
            future.join();
        } catch (Exception e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException("Failed streaming download: " + resource.fileName(), e);
        }
    }

    /**
     * Initiates asynchronous streaming on a dedicated Virtual Thread.
     *
     * @param exchange active HTTP exchange
     * @param resource downloadable resource descriptor
     * @return CompletableFuture completing when response transmission finishes
     */
    public static CompletableFuture<Void> sendDownloadAsync(HttpExchange exchange, DownloadResource resource) {
        Objects.requireNonNull(exchange, "HttpExchange must not be null");
        Objects.requireNonNull(resource, "DownloadResource must not be null");

        CompletableFuture<Void> future = new CompletableFuture<>();

        Thread.ofVirtual()
            .name("jettra-download-" + resource.fileName())
            .start(() -> {
                Throwable error = null;
                try {
                    streamResource(exchange, resource);
                } catch (Throwable t) {
                    error = t;
                } finally {
                    performCleanup(resource);
                }

                if (error != null) {
                    future.completeExceptionally(error);
                } else {
                    future.complete(null);
                }
            });

        return future;
    }

    private static void streamResource(HttpExchange exchange, DownloadResource resource) throws IOException {
        String safeName = DownloadSecurity.sanitizeFileName(resource.fileName());
        String contentType = resource.contentType();
        long length = resource.contentLength();

        // Standard HTTP download attachment headers
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + safeName + "\"");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.getResponseHeaders().set("Pragma", "no-cache");
        exchange.getResponseHeaders().set("Expires", "0");

        if (length >= 0) {
            exchange.sendResponseHeaders(200, length);
        } else {
            // 0 triggers chunked transfer encoding in com.sun.net.httpserver
            exchange.sendResponseHeaders(200, 0);
        }

        try (InputStream in = resource.openStream();
             OutputStream out = exchange.getResponseBody()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    /**
     * Executes lifecycle cleanup for ephemeral resources using Java 25 pattern matching for switch.
     */
    private static void performCleanup(DownloadResource resource) {
        if (!resource.isEphemeral()) {
            return;
        }

        switch (resource) {
            case DownloadResource.PathDownloadResource pathRes when pathRes.isEphemeral() -> {
                try {
                    Files.deleteIfExists(pathRes.path());
                } catch (IOException ignored) {}
            }
            case DownloadResource.PathDownloadResource ignored -> {}
            case DownloadResource.StreamDownloadResource ignored -> {}
            case DownloadResource.ByteArrayDownloadResource ignored -> {}
            case DownloadResource.StringDownloadResource ignored -> {}
        }
    }
}
