package com.coremedia.iso;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class IsoTypeWriterVariable {

    public static void write(long v, ByteBuffer bb, int bytes) {
        switch (bytes) {
            case 1:
                IsoTypeWriter.writeUInt8(bb, (int) (v & 0xff));
                break;
            case 2:
                IsoTypeWriter.writeUInt16(bb, (int) (v & 0xffff));
                break;
            case 3:
                IsoTypeWriter.writeUInt24(bb, (int) (v & 0xffffff));
                break;
            case 4:
                IsoTypeWriter.writeUInt32(bb, v);
                break;
            case 8:
                IsoTypeWriter.writeUInt64(bb, v);
                break;
            default:
                throw new RuntimeException("I don't know how to read " + bytes + " bytes");
        }

    }
}
