package io.jettra.server.jakarta.servlet;

import io.jettra.server.core.http.HttpResponse;
import io.jettra.server.core.http.HttpStatus;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HttpServletResponse {

    private final HttpResponse delegate;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PrintWriter writer;

    public HttpServletResponse(HttpResponse delegate) {
        this.delegate = delegate;
    }

    public void setStatus(int sc) {
        delegate.status(sc);
    }

    public void setHeader(String name, String value) {
        delegate.header(name, value);
    }

    public void setContentType(String type) {
        delegate.contentType(type);
    }

    public PrintWriter getWriter() {
        if (writer == null) {
            writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
        }
        return writer;
    }

    public void sendError(int sc, String msg) {
        delegate.status(sc).body(msg);
    }

    public void sendRedirect(String location) {
        delegate.status(HttpStatus.FOUND).header("Location", location);
    }

    public void flushBuffer() {
        if (writer != null) {
            writer.flush();
        }
        if (outputStream.size() > 0) {
            delegate.body(outputStream.toByteArray());
        }
    }

    public HttpResponse getDelegate() {
        flushBuffer();
        return delegate;
    }
}
