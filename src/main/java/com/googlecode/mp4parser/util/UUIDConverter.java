package com.googlecode.mp4parser.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * UUID from/to byte array.
 */
public class UUIDConverter {
    public static byte[] convert(UUID uuid) {

        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;

    }

    public static UUID convert(byte[] uuidBytes) {
        ByteBuffer b = ByteBuffer.wrap(uuidBytes);
        b.order(ByteOrder.BIG_ENDIAN);
        return new UUID(b.getLong(), b.getLong());
    }
}
