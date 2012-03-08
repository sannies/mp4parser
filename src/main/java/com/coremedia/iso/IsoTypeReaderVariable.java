package com.coremedia.iso;

import java.nio.ByteBuffer;

public final class IsoTypeReaderVariable {

    public static long read(ByteBuffer bb, int bytes) {
        switch (bytes) {
            case 1:
                return IsoTypeReader.readUInt8(bb);
            case 2:
                return IsoTypeReader.readUInt16(bb);
            case 3:
                return IsoTypeReader.readUInt24(bb);
            case 4:
                return IsoTypeReader.readUInt32(bb);
            case 8:
                return IsoTypeReader.readUInt64(bb);
            default:
                throw new RuntimeException("I don't know how to read " + bytes + " bytes");
        }

    }
}
