package io.jettra.jwt;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Native JWT implementation for the Jettra stack.
 * No external dependencies.
 * @author avbravo
 */
public class JettraJWT {

    private final String secret;
    private final long expirationTime;

    public JettraJWT() {
        this(resolveDefaultSecret(), resolveDefaultExpiration());
    }

    public JettraJWT(String secret, long expirationTimeMillis) {
        this.secret = (secret != null && !secret.isBlank()) ? secret : resolveDefaultSecret();
        this.expirationTime = expirationTimeMillis > 0 ? expirationTimeMillis : resolveDefaultExpiration();
    }

    private static String resolveDefaultSecret() {
        String sec = System.getProperty("server.JWT_SECRET");
        if (sec != null && !sec.isBlank()) return sec.trim();
        sec = System.getenv("SERVER_JWT_SECRET");
        if (sec != null && !sec.isBlank()) return sec.trim();

        // Leer desde jettra-config.properties o jettra-rest.properties
        String[] files = {"jettra-config.properties", "jettra-rest.properties"};
        for (String file : files) {
            try (InputStream is = JettraJWT.class.getClassLoader().getResourceAsStream(file)) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    String v = props.getProperty("server.JWT_SECRET", props.getProperty("server.jwt.secret", props.getProperty("jettra.rest.security.jwt.secret")));
                    if (v != null && !v.isBlank()) return v.trim();
                }
            } catch (Exception ignored) {}
        }
        return "default_secret_key_jettra_rest_2026";
    }

    private static long resolveDefaultExpiration() {
        String exp = System.getProperty("server.JWT_EXPIRATION");
        if (exp != null && !exp.isBlank()) {
            try { return Long.parseLong(exp.trim()); } catch (Exception ignored) {}
        }
        exp = System.getenv("SERVER_JWT_EXPIRATION");
        if (exp != null && !exp.isBlank()) {
            try { return Long.parseLong(exp.trim()); } catch (Exception ignored) {}
        }

        String[] files = {"jettra-config.properties", "jettra-rest.properties"};
        for (String file : files) {
            try (InputStream is = JettraJWT.class.getClassLoader().getResourceAsStream(file)) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    String v = props.getProperty("server.JWT_EXPIRATION", props.getProperty("server.jwt.expiration", props.getProperty("jettra.rest.security.jwt.expiration-millis")));
                    if (v != null && !v.isBlank()) {
                        try { return Long.parseLong(v.trim()); } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }
        return 3600000L; // 1 hora por defecto
    }

    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        if (roles != null) {
            claims.put("roles", roles);
            claims.put("groups", roles);
        }
        return generateToken(claims, username);
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>(extraClaims != null ? extraClaims : Collections.emptyMap());
        payload.put("sub", username);
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() + expirationTime) / 1000);

        String encodedHeader = base64UrlEncode(JettraJson.toJson(header));
        String encodedPayload = base64UrlEncode(JettraJson.toJson(payload));

        String signature = sign(encodedHeader + "." + encodedPayload, secret);
        
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public boolean isTokenValid(String token, String username) {
        try {
            if (token == null || token.isBlank()) return false;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String headerPayload = parts[0] + "." + parts[1];
            String signature = parts[2];

            if (!signature.equals(sign(headerPayload, secret))) {
                return false;
            }

            Map<String, Object> payload = JettraJson.parse(base64UrlDecode(parts[1]));
            String subject = (String) payload.get("sub");
            long exp = ((Number) payload.get("exp")).longValue();

            return (username == null || subject.equals(username)) && exp * 1000 > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        return isTokenValid(token, null);
    }

    public String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        Map<String, Object> payload = getPayload(token);
        return (String) payload.get("sub");
    }

    public List<String> extractRoles(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        List<String> roles = new ArrayList<>();
        try {
            Map<String, Object> payload = getPayload(token);
            Object rolesClaim = payload.get("roles");
            if (rolesClaim == null) rolesClaim = payload.get("groups");

            if (rolesClaim instanceof List<?> list) {
                for (Object r : list) {
                    if (r != null) roles.add(r.toString());
                }
            } else if (rolesClaim instanceof String str) {
                if (str.startsWith("[") && str.endsWith("]")) {
                    str = str.substring(1, str.length() - 1);
                }
                for (String s : str.split(",")) {
                    if (!s.trim().isEmpty()) roles.add(s.trim());
                }
            }
        } catch (Exception ignored) {}
        return roles;
    }

    public Date extractExpiration(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        Map<String, Object> payload = getPayload(token);
        long exp = ((Number) payload.get("exp")).longValue();
        return new Date(exp * 1000);
    }

    public Map<String, Object> getPayload(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        String[] parts = token.split("\\.");
        return JettraJson.parse(base64UrlDecode(parts[1]));
    }

    private String sign(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return base64UrlEncode(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    private String base64UrlEncode(String data) {
        return base64UrlEncode(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String base64UrlDecode(String data) {
        return new String(Base64.getUrlDecoder().decode(data), StandardCharsets.UTF_8);
    }

    public String getSecret() {
        return secret;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
