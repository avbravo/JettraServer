# JettraGRPC - Comprehensive Guide & Architecture Manual

## 1. Overview & Architecture
`JettraGRPC` is the high-performance RPC and binary protocol communication module used by Jettra clusters for Raft log consensus, inter-node heartbeat synchronization, and low-latency database replication.

---

## 2. Key Features
- **High-Speed RPC Protocol**: Binary protocol framing for sub-millisecond node-to-node replication.
- **Raft Consensus Replication**: Synchronous and asynchronous log replication with quorum voting.
- **Virtual Thread Dispatching**: Efficient handling of concurrent streaming channels.
- **TLS / mTLS Encryption**: Secure communication between distributed cluster nodes.

---

## 3. Installation
```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>JettraGRPC</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. Usage & Code Examples

### 4.1 Initializing a gRPC Replication Server & Client
```java
import io.jettra.grpc.JettraGrpcServer;
import io.jettra.grpc.JettraGrpcClient;

public class GrpcDemo {
    public static void main(String[] args) throws Exception {
        // 1. Start Server on port 50051
        JettraGrpcServer server = new JettraGrpcServer(50051);
        server.registerHandler("RAFT_REPLICATE", (payload) -> {
            System.out.println("Received Raft log entry: " + new String(payload));
            return "ACK".getBytes();
        });
        server.start();

        // 2. Connect Client
        JettraGrpcClient client = new JettraGrpcClient("localhost", 50051);
        client.connect();
        
        byte[] response = client.send("RAFT_REPLICATE", "PUT rec:employees:101 {...}".getBytes());
        System.out.println("Replication response: " + new String(response));
        
        client.close();
        server.stop();
    }
}
```
