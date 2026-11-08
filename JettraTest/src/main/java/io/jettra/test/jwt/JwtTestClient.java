package io.jettra.test.jwt;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Utility client to perform JWT authentication and subsequent requests for JettraTest.
 */
public class JwtTestClient {

    private final HttpClient httpClient;
    private String token;

    public JwtTestClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Authenticates with the given endpoint and payload, and stores the token.
     * Extracts token from response body or headers depending on implementation.
     * We will assume the endpoint returns a raw token string or it is JSON and we grab the whole thing for simplicity
     * or we expect the user to parse it. Let's return the body.
     */
    public String authenticate(String url, String jsonPayload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String respBody = response.body();
            // Basic extraction if the response is JSON like {"token":"Bearer eyJ..."}
            if (respBody.trim().startsWith("{") && respBody.contains("\"token\"")) {
                int start = respBody.indexOf("\"token\"");
                int colon = respBody.indexOf(":", start);
                int quoteStart = respBody.indexOf("\"", colon);
                int quoteEnd = respBody.indexOf("\"", quoteStart + 1);
                if (quoteStart != -1 && quoteEnd != -1) {
                    respBody = respBody.substring(quoteStart + 1, quoteEnd);
                }
            }
            // Remove "Bearer " if it's already there because getWithToken adds it
            if (respBody.startsWith("Bearer ")) {
                respBody = respBody.substring(7);
            }
            this.token = respBody;
            return this.token;
        } else {
            throw new RuntimeException("Authentication failed with status " + response.statusCode() + ": " + response.body());
        }
    }

    /**
     * Sets the token manually.
     */
    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    /**
     * Performs a GET request using the stored JWT token.
     */
    public String getWithToken(String url) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("JWT Token is not set. Authenticate first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("GET request failed with status " + response.statusCode() + ": " + response.body());
        }
    }
    /**
     * Performs a POST request using the stored JWT token.
     */
    public String postWithToken(String url, String jsonBody) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("JWT Token is not set. Authenticate first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("POST request failed with status " + response.statusCode() + ": " + response.body());
        }
    }

    /**
     * Performs a PUT request using the stored JWT token.
     */
    public String putWithToken(String url, String jsonBody) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("JWT Token is not set. Authenticate first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("PUT request failed with status " + response.statusCode() + ": " + response.body());
        }
    }

    /**
     * Performs a DELETE request using the stored JWT token.
     */
    public String deleteWithToken(String url) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("JWT Token is not set. Authenticate first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("DELETE request failed with status " + response.statusCode() + ": " + response.body());
        }
    }
}
