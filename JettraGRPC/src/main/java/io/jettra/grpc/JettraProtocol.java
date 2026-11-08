package io.jettra.grpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * JettraProtocol implements the gRPC wire protocol framing.
 * [1 byte compressed flag] [4 bytes length] [payload]
 */
public class JettraProtocol {

    public static void writeFrame(OutputStream out, byte[] payload) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(0); // No compression for now
        dos.writeInt(payload.length);
        dos.write(payload);
        dos.flush();
    }

    public static byte[] readFrame(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int compression = dis.readByte();
        int length = dis.readInt();
        byte[] payload = new byte[length];
        dis.readFully(payload);
        return payload;
    }
}
