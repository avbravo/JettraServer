package io.jettra.flux.download;

import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.TempDir;
import io.jettra.test.annotation.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
class DownloadResourceTest {

    @Test
    @DisplayName("PathDownloadResource reads file data and metadata accurately")
    void testPathDownloadResource(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("backup-snapshot.md");
        String content = "# JettraDB Telemetry Snapshot\nStatus: Healthy";
        Files.writeString(file, content, StandardCharsets.UTF_8);

        DownloadResource res = DownloadResource.ofPath(file);
        assertEquals("backup-snapshot.md", res.fileName());
        assertEquals("text/markdown; charset=UTF-8", res.contentType());
        assertEquals(content.getBytes(StandardCharsets.UTF_8).length, res.contentLength());
        assertFalse(res.isEphemeral());

        try (InputStream in = res.openStream()) {
            String read = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, read);
        }
    }

    @Test
    @DisplayName("StringDownloadResource handles Markdown text with proper UTF-8 length")
    void testStringDownloadResource() throws IOException {
        String md = "# Title\n* Metric: 100";
        DownloadResource res = DownloadResource.ofMarkdown("report.md", md);

        assertEquals("report.md", res.fileName());
        assertEquals("text/markdown; charset=UTF-8", res.contentType());
        assertEquals(md.getBytes(StandardCharsets.UTF_8).length, res.contentLength());

        try (InputStream in = res.openStream()) {
            String read = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(md, read);
        }
    }

    @Test
    @DisplayName("ByteArrayDownloadResource streams byte data cleanly")
    void testByteArrayDownloadResource() throws IOException {
        byte[] bytes = new byte[] { 0x10, 0x20, 0x30, 0x40 };
        DownloadResource res = DownloadResource.ofBytes("data.bin", bytes, "application/octet-stream");

        assertEquals("data.bin", res.fileName());
        assertEquals(4, res.contentLength());
        assertArrayEquals(bytes, res.openStream().readAllBytes());
    }

    @Test
    @DisplayName("StreamDownloadResource supplies new stream upon each open")
    void testStreamDownloadResource() throws IOException {
        byte[] payload = "Streaming telemetry payload".getBytes(StandardCharsets.UTF_8);
        DownloadResource res = DownloadResource.ofStream(
            "stream.txt",
            () -> new ByteArrayInputStream(payload),
            "text/plain",
            payload.length,
            true
        );

        assertEquals("stream.txt", res.fileName());
        assertTrue(res.isEphemeral());
        try (InputStream in1 = res.openStream();
             InputStream in2 = res.openStream()) {
            assertArrayEquals(payload, in1.readAllBytes());
            assertArrayEquals(payload, in2.readAllBytes());
        }
    }

    @Test
    @DisplayName("Java 25 Pattern Matching for switch exhaustively resolves DownloadResource")
    void testPatternMatchingForSwitch() {
        DownloadResource res1 = DownloadResource.ofMarkdown("test.md", "# MD");
        DownloadResource res2 = DownloadResource.ofBytes("test.bin", new byte[0], "application/octet-stream");

        String type1 = switch (res1) {
            case DownloadResource.StringDownloadResource s -> "STRING:" + s.fileName();
            case DownloadResource.ByteArrayDownloadResource b -> "BYTE:" + b.fileName();
            case DownloadResource.PathDownloadResource p -> "PATH:" + p.fileName();
            case DownloadResource.StreamDownloadResource st -> "STREAM:" + st.fileName();
        };
        assertEquals("STRING:test.md", type1);

        String type2 = switch (res2) {
            case DownloadResource.StringDownloadResource s -> "STRING:" + s.fileName();
            case DownloadResource.ByteArrayDownloadResource b -> "BYTE:" + b.fileName();
            case DownloadResource.PathDownloadResource p -> "PATH:" + p.fileName();
            case DownloadResource.StreamDownloadResource st -> "STREAM:" + st.fileName();
        };
        assertEquals("BYTE:test.bin", type2);
    }
}
