# JettraJWT - Comprehensive Guide & Architecture Manual

## 1. Overview & Architecture
`JettraJWT` provides enterprise JSON Web Token (JWT) generation, validation, cryptographic signing, and role-based claim extraction for the Jettra ecosystem. It ensures secure zero-trust communication across microservices, REST APIs, and database engines.

---

## 2. Key Features
- **HMAC-SHA256 & RSA Cryptographic Signing**: Secure verification of claims and tokens.
- **Role and Claim Extraction**: Granular per-database and engine permissions (`DB_ADMIN`, `READ_WRITE`, `READ_ONLY`, `MANAGER`).
- **Zero-Dependency Security Core**: Standard Java Cryptography Architecture (JCA) implementation.
- **Automatic Expiration & Renewal**: Built-in validation of `nbf`, `exp`, and `iat` claims.

---

## 3. Installation
```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>JettraJWT</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. Usage & Code Examples

### 4.1 Token Generation & Verification
```java
import io.jettra.jwt.JettraJwt;
import io.jettra.jwt.JwtClaims;

public class JwtDemo {
    public static void main(String[] args) {
        String secretKey = "super-secret-key-that-is-at-least-32-chars-long";
        JettraJwt jwt = new JettraJwt(secretKey);

        // 1. Generate Token
        JwtClaims claims = new JwtClaims();
        claims.setSubject("carlos");
        claims.setIssuer("JettraStoreEngine");
        claims.put("roles", "DB_ADMIN,MANAGER");
        claims.put("database", "invoicing_db");

        String token = jwt.generateToken(claims, 3600); // 1 hour expiration
        System.out.println("Generated JWT: " + token);

        // 2. Validate Token
        boolean valid = jwt.validateToken(token);
        if (valid) {
            JwtClaims extracted = jwt.parseClaims(token);
            System.out.println("User: " + extracted.getSubject());
            System.out.println("Roles: " + extracted.get("roles"));
        }
    }
}
```
