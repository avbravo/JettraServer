package io.jettra.server.core.http;

import io.jettra.json.JettraJson;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Representación inmutable y asíncrona de una petición HTTP entrante.
 */
public class HttpRequest {

    private final HttpMethod method;
    private final String uri;
    private final String path;
    private final String queryString;
    private final Map<String, String> queryParams;
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, String> headers;
    private final byte[] body;
    private final SocketAddress remoteAddress;
    private final Map<String, Object> attributes = new HashMap<>();

    public HttpRequest(HttpMethod method, String rawUri, Map<String, String> rawHeaders, byte[] body, SocketAddress remoteAddress) {
        this.method = method;
        this.uri = rawUri;
        this.body = body != null ? body : new byte[0];
        this.remoteAddress = remoteAddress;

        // Separar ruta y query params
        int queryIdx = rawUri.indexOf('?');
        if (queryIdx >= 0) {
            this.path = rawUri.substring(0, queryIdx);
            this.queryString = rawUri.substring(queryIdx + 1);
            this.queryParams = parseQueryParams(this.queryString);
        } else {
            this.path = rawUri;
            this.queryString = "";
            this.queryParams = Collections.emptyMap();
        }

        // Encabezados case-insensitive
        Map<String, String> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (rawHeaders != null) {
            caseInsensitiveHeaders.putAll(rawHeaders);
        }
        this.headers = Collections.unmodifiableMap(caseInsensitiveHeaders);
    }

    private static Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isBlank()) return Collections.emptyMap();
        Map<String, String> result = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String k = java.net.URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                String v = java.net.URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                result.put(k, v);
            } else if (!pair.isBlank()) {
                result.put(java.net.URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public HttpMethod getMethod() { return method; }
    public String getUri() { return uri; }
    public String getPath() { return path; }
    public String getQueryString() { return queryString; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public String getQueryParam(String name) { return queryParams.get(name); }

    public Map<String, String> getPathParams() { return Collections.unmodifiableMap(pathParams); }
    public String getPathParam(String name) { return pathParams.get(name); }
    public void setPathParam(String name, String value) { pathParams.put(name, value); }
    public void setPathParams(Map<String, String> params) { pathParams.putAll(params); }

    public Map<String, String> getHeaders() { return headers; }
    public String getHeader(String name) { return headers.get(name); }

    private static final JettraJson JSON_SERIALIZER = new JettraJson();

    public byte[] getBody() { return body; }
    public String getBodyAsString() { return new String(body, StandardCharsets.UTF_8); }

    public <T> T getBodyAsJson(Class<T> type) {
        String json = getBodyAsString();
        if (json.isBlank()) return null;
        return JSON_SERIALIZER.fromJson(json, type);
    }

    public SocketAddress getRemoteAddress() { return remoteAddress; }

    public Object getAttribute(String name) { return attributes.get(name); }
    public void setAttribute(String name, Object value) { attributes.put(name, value); }
    public Map<String, Object> getAttributes() { return Collections.unmodifiableMap(attributes); }
}
