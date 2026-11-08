package io.jettra.grpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Jettra-native simplified Protobuf binary serializer.
 * Supports basic types (Varint, Length-delimited).
 */
public class JettraSerializer {

    public static byte[] encodeVarint(long value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((value & ~0x7F) != 0) {
            out.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.write((int) value);
        return out.toByteArray();
    }

    public static long decodeVarint(byte[] data, int[] offset) {
        long result = 0;
        int shift = 0;
        while (offset[0] < data.length) {
            byte b = data[offset[0]++];
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return result;
            shift += 7;
        }
        return result;
    }

    // Additional encoding/decoding for strings and bytes would go here
}
