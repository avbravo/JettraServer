# JettraGRPC: Native gRPC Bridge for JettraServer

JettraGRPC is a lightweight, high-performance library that implements a native gRPC-compatible wire protocol without the heavy overhead of Netty or external Protobuf libraries. It is designed to work seamlessly with **JettraServer** and **Java 25 Virtual Threads**.

## 🏗️ Architecture

The library is built on a modular architecture that separates the transport, protocol framing, and serialization layers:

1.  **Transport Layer**: Uses standard JDK `HttpServer` (Server-side) and `HttpClient` (Client-side).
2.  **Protocol Layer (`JettraProtocol`)**: Implements the gRPC binary framing format:
    - `[1-byte compression flag] [4-bytes message length] [Payload]`
3.  **Serialization Layer (`JettraSerializer`)**: Provides a native implementation of Varint and length-delimited encoding, allowing for Protobuf-wire compatibility without the `protobuf-java` library.
4.  **Observer Pattern (`JettraObserver`)**: Implements an asynchronous streaming API similar to `StreamObserver`.

## 🚀 Functionality

- **Native gRPC Framing**: Fully compatible with the binary structure of gRPC calls.
- **Zero-Netty Footprint**: Optimized for low-memory environments and Java Virtual Threads.
- **Handler Integration**: Provides `HttpHandler` instances that can be registered in any `JettraServer`.
- **Lightweight Serialization**: Native support for common types used in Jettra services.

## 💻 Usage Examples

### Server-side Registration
```java
JettraServer server = new JettraServer();
MyService myService = new MyServiceImpl();

// Register the service using the JettraGRPC bridge
server.addHandler("/MyService", JettraGRPCServer.createHandler(myService));
server.start();
```

### Client-side Call
```java
JettraGRPCClient client = new JettraGRPCClient("localhost", 8080);
byte[] payload = mySerializer.serialize(request);

byte[] response = client.call("MyService", "MyMethod", payload);
```

## 🛠️ Build Requirements
- **Java 25** (with experimental flags)
- **Maven 3.x**
- No external runtime dependencies (except JettraServer for the consuming project).
