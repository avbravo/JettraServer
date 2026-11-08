# JettraJSON - Comprehensive Guide & Architecture Manual

## 1. Overview & Architecture
`JettraJSON` is an ultra-fast, zero-dependency JSON serialization, parsing, and data mapping engine written natively in **Java 25**. It powers document serialization, REST payloads, and Java 25 Record reflection across the entire Jettra stack.

---

## 2. Key Features
- **Java 25 Records First-Class Reflection**: Introspects and instantiates canonical record constructors with zero boilerplate.
- **Lightweight Document Model**: High-performance `JsonObject` and `JsonArray` hierarchy without heavy third-party dependencies.
- **Fast Type Parsing**: Primitive, Object, and generic Collection mapping with automatic null safety.
- **Zero Reflection Caching Overhead**: Optimized bytecode access paths for high-throughput streaming.

---

## 3. Installation
```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>JettraJSON</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. Usage & Code Examples

### 4.1 Serializing and Deserializing Java Records
```java
import io.jettra.json.JettraJson;

public record UserProfile(String username, String email, int age, boolean active) {}

public class JsonDemo {
    public static void main(String[] args) {
        JettraJson json = new JettraJson();

        // 1. Record to JSON
        UserProfile profile = new UserProfile("jdoe", "jdoe@example.com", 30, true);
        String jsonStr = json.toJson(profile);
        System.out.println("JSON: " + jsonStr);

        // 2. JSON to Record
        UserProfile deserialized = json.fromJson(jsonStr, UserProfile.class);
        System.out.println("Username: " + deserialized.username() + " (" + deserialized.age() + ")");
    }
}
```

### 4.2 Fluent Dynamic Document Manipulation
```java
import io.jettra.json.JsonObject;
import io.jettra.json.JsonArray;

public class DynamicJsonDemo {
    public static void main(String[] args) {
        JsonObject root = new JsonObject();
        root.addProperty("id", "REC-900");
        root.addProperty("title", "Invoice Analysis");
        root.addProperty("score", 98.5);

        JsonArray tags = new JsonArray();
        tags.add("finance");
        tags.add("audit");
        root.add("tags", tags);

        System.out.println(root.toString());
    }
}
```
