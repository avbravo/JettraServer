package io.jettra.flux.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Sealed representation of a downloadable resource in Java 25+.
 * Supports Path, Stream, ByteArray, and String (Markdown/Text) sources
 * with immutable records and pattern matching for switch.
 */
public sealed interface DownloadResource permits
    DownloadResource.PathDownloadResource,
    DownloadResource.StreamDownloadResource,
    DownloadResource.ByteArrayDownloadResource,
    DownloadResource.StringDownloadResource {

    String fileName();
    String contentType();
    long contentLength();
    InputStream openStream() throws IOException;
    boolean isEphemeral();
    default boolean ephemeral() {
        return isEphemeral();
    }

    @FunctionalInterface
    interface InputStreamSupplier {
        InputStream get() throws IOException;
    }

    /**
     * Path-backed downloadable resource.
     */
    record PathDownloadResource(
        Path path,
        String fileName,
        String contentType,
        boolean ephemeral
    ) implements DownloadResource {

        public PathDownloadResource {
            Objects.requireNonNull(path, "path must not be null");
            fileName = DownloadSecurity.sanitizeFileName(fileName != null ? fileName : path.getFileName().toString());
            contentType = (contentType != null && !contentType.isBlank()) ? contentType : "application/octet-stream";
        }

        @Override
        public boolean isEphemeral() {
            return ephemeral;
        }

        @Override
        public long contentLength() {
            try {
                return Files.exists(path) ? Files.size(path) : -1L;
            } catch (IOException e) {
                return -1L;
            }
        }

        @Override
        public InputStream openStream() throws IOException {
            return Files.newInputStream(path);
        }
    }

    /**
     * Stream-backed downloadable resource with supplier.
     */
    record StreamDownloadResource(
        String fileName,
        InputStreamSupplier streamSupplier,
        String contentType,
        long contentLength,
        boolean ephemeral
    ) implements DownloadResource {

        public StreamDownloadResource {
            fileName = DownloadSecurity.sanitizeFileName(fileName);
            Objects.requireNonNull(streamSupplier, "streamSupplier must not be null");
            contentType = (contentType != null && !contentType.isBlank()) ? contentType : "application/octet-stream";
        }

        @Override
        public boolean isEphemeral() {
            return ephemeral;
        }

        @Override
        public InputStream openStream() throws IOException {
            return streamSupplier.get();
        }
    }

    /**
     * In-memory byte-array backed downloadable resource.
     */
    record ByteArrayDownloadResource(
        String fileName,
        byte[] data,
        String contentType
    ) implements DownloadResource {

        public ByteArrayDownloadResource {
            fileName = DownloadSecurity.sanitizeFileName(fileName);
            Objects.requireNonNull(data, "data must not be null");
            contentType = (contentType != null && !contentType.isBlank()) ? contentType : "application/octet-stream";
        }

        @Override
        public long contentLength() {
            return data.length;
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public boolean isEphemeral() {
            return false;
        }
    }

    /**
     * String-backed downloadable resource (ideal for Markdown, JSON, CSV).
     */
    record StringDownloadResource(
        String fileName,
        String content,
        String contentType
    ) implements DownloadResource {

        public StringDownloadResource {
            fileName = DownloadSecurity.sanitizeFileName(fileName);
            Objects.requireNonNull(content, "content must not be null");
            contentType = (contentType != null && !contentType.isBlank()) ? contentType : "text/plain; charset=UTF-8";
        }

        @Override
        public long contentLength() {
            return content.getBytes(StandardCharsets.UTF_8).length;
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public boolean isEphemeral() {
            return false;
        }
    }

    // --- Fluent Factory Methods ---

    static DownloadResource ofPath(Path path, String contentType, boolean ephemeral) {
        return new PathDownloadResource(path, path.getFileName().toString(), contentType, ephemeral);
    }

    static DownloadResource ofPath(Path path, String contentType) {
        return ofPath(path, contentType, false);
    }

    static DownloadResource ofPath(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        String mime = name.endsWith(".md") || name.endsWith(".markdown") ? "text/markdown; charset=UTF-8"
                    : name.endsWith(".json") ? "application/json; charset=UTF-8"
                    : name.endsWith(".csv") ? "text/csv; charset=UTF-8"
                    : name.endsWith(".txt") ? "text/plain; charset=UTF-8"
                    : "application/octet-stream";
        return ofPath(path, mime, false);
    }

    static DownloadResource ofBytes(String fileName, byte[] data, String contentType) {
        return new ByteArrayDownloadResource(fileName, data, contentType);
    }

    static DownloadResource ofString(String fileName, String content, String contentType) {
        return new StringDownloadResource(fileName, content, contentType);
    }

    static DownloadResource ofMarkdown(String fileName, String markdownContent) {
        return new StringDownloadResource(fileName, markdownContent, "text/markdown; charset=UTF-8");
    }

    static DownloadResource ofStream(
        String fileName,
        InputStreamSupplier streamSupplier,
        String contentType,
        long contentLength,
        boolean ephemeral
    ) {
        return new StreamDownloadResource(fileName, streamSupplier, contentType, contentLength, ephemeral);
    }
}
