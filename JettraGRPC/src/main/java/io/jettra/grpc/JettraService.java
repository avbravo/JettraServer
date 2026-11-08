package io.jettra.grpc;

/**
 * Base interface for Jettra-native gRPC services.
 */
public interface JettraService {
    String getServiceName();

    /**
     * Optional optimized entry point for raw binary gRPC calls.
     * Services can override this to avoid multiple layers of serialization.
     */
    default void call(String methodName, byte[] requestData, JettraObserver<byte[]> responseObserver) {
        // Default implementation does nothing or could throw an exception
        responseObserver.onError(new UnsupportedOperationException("Method " + methodName + " not implemented in " + getServiceName()));
    }
}
