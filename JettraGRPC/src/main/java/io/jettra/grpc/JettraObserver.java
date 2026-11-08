package io.jettra.grpc;

/**
 * Jettra-native replacement for io.grpc.stub.StreamObserver.
 * Provides a simple interface for streaming data.
 */
public interface JettraObserver<T> {
    void onNext(T value);
    void onError(Throwable t);
    void onCompleted();
}
