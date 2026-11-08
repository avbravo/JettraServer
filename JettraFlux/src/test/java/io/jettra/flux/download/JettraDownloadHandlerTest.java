package io.jettra.flux.download;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.TempDir;
import io.jettra.test.annotation.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
class JettraDownloadHandlerTest {

    @Test
    @DisplayName("sendDownload streams Markdown with attachment headers using Virtual Threads")
    void testSendDownloadMarkdown() throws IOException {
        MockHttpExchange exchange = new MockHttpExchange();
        String content = "# Backup Snapshot\n- Timestamp: 2026-09-03\n- Health: 100%";
        DownloadResource resource = DownloadResource.ofMarkdown("snapshot-2026.md", content);

        JettraDownloadHandler.sendDownload(exchange, resource);

        assertEquals(200, exchange.getResponseCode());
        assertEquals("text/markdown; charset=UTF-8", exchange.getResponseHeaders().getFirst("Content-Type"));
        assertEquals("attachment; filename=\"snapshot-2026.md\"", exchange.getResponseHeaders().getFirst("Content-Disposition"));
        assertEquals("no-cache, no-store, must-revalidate", exchange.getResponseHeaders().getFirst("Cache-Control"));
        assertEquals(content, exchange.getResponseBodyAsString());
    }

    @Test
    @DisplayName("sendDownload cleans up ephemeral PathDownloadResource after transmission")
    void testEphemeralPathCleanup(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("ephemeral-snapshot.md");
        Files.writeString(tempFile, "Ephemeral content", StandardCharsets.UTF_8);
        assertTrue(Files.exists(tempFile));

        DownloadResource resource = DownloadResource.ofPath(tempFile, "text/markdown; charset=UTF-8", true);
        assertTrue(resource.isEphemeral());

        MockHttpExchange exchange = new MockHttpExchange();
        JettraDownloadHandler.sendDownload(exchange, resource);

        assertEquals(200, exchange.getResponseCode());
        assertEquals("Ephemeral content", exchange.getResponseBodyAsString());
        // Verify ephemeral file was removed
        assertFalse(Files.exists(tempFile), "Ephemeral file should be deleted after streaming");
    }

    @Test
    @DisplayName("sendDownload handles non-ephemeral Path without deletion")
    void testNonEphemeralPathPreserved(@TempDir Path tempDir) throws IOException {
        Path permanentFile = tempDir.resolve("permanent-snapshot.md");
        Files.writeString(permanentFile, "Permanent content", StandardCharsets.UTF_8);

        DownloadResource resource = DownloadResource.ofPath(permanentFile);
        assertFalse(resource.isEphemeral());

        MockHttpExchange exchange = new MockHttpExchange();
        JettraDownloadHandler.sendDownload(exchange, resource);

        assertEquals(200, exchange.getResponseCode());
        assertEquals("Permanent content", exchange.getResponseBodyAsString());
        assertTrue(Files.exists(permanentFile), "Permanent file must be retained on disk");
    }

    private static class MockHttpExchange extends HttpExchange {
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;

        @Override public Headers getRequestHeaders() { return requestHeaders; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public URI getRequestURI() { return URI.create("/dashboard?action=download"); }
        @Override public String getRequestMethod() { return "GET"; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return new ByteArrayInputStream(new byte[0]); }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override public void sendResponseHeaders(int rCode, long responseLength) { this.responseCode = rCode; }
        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress("127.0.0.1", 8080); }
        @Override public int getResponseCode() { return responseCode; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress("127.0.0.1", 8080); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public HttpPrincipal getPrincipal() { return null; }

        public String getResponseBodyAsString() {
            return responseBody.toString(StandardCharsets.UTF_8);
        }
    }
}
