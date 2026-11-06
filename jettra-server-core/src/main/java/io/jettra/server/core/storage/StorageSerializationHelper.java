package io.jettra.server.core.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utilidad de serialización y deserialización binaria de alto rendimiento para motores Off-Heap y Disco.
 */
public final class StorageSerializationHelper {

    private StorageSerializationHelper() {}

    public static byte[] serialize(Object value) {
        if (value == null) return new byte[0];
        if (value instanceof byte[] b) return b;
        if (value instanceof String s) return s.getBytes(StandardCharsets.UTF_8);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(value);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            // Fallback a String si no es serializable
            return value.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> V deserialize(byte[] bytes, Class<V> targetType) {
        if (bytes == null || bytes.length == 0) return null;
        if (targetType == byte[].class) {
            return (V) bytes;
        }
        if (targetType == String.class) {
            return (V) new String(bytes, StandardCharsets.UTF_8);
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (V) ois.readObject();
        } catch (Exception e) {
            // Fallback a String
            return (V) new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
