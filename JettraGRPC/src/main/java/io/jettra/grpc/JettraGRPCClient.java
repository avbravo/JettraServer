package io.jettra.grpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

/**
 * Jettra-native gRPC client implementation.
 * Uses Java 25 HttpClient for transport.
 */
public class JettraGRPCClient {
    private static final Logger logger = Logger.getLogger(JettraGRPCClient.class.getName());
    private final HttpClient httpClient;
    private final String baseUrl;

    public JettraGRPCClient(String host, int port) {
        this.baseUrl = "http://" + host + ":" + port;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public byte[] call(String serviceName, String methodName, byte[] requestData) throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JettraProtocol.writeFrame(baos, requestData);
        byte[] frame = baos.toByteArray();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + serviceName + "/" + methodName))
                .header("Content-Type", "application/grpc+jettra")
                .POST(HttpRequest.BodyPublishers.ofByteArray(frame))
                .build();

        logger.info("Sending Jettra-native gRPC request to " + serviceName + "/" + methodName);
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            // Read response frame
            // Simplification: assume single frame for now
            return response.body(); // Need to strip framing if it's there
        } else {
            throw new IOException("JettraGRPC call failed with status: " + response.statusCode());
        }
    }
}
