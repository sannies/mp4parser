package com.coremedia.iso;

/**
 * Encapsulates access to underlying storage.
 */
public interface IsoBufferWrapper {
    int readUInt8();

    int readUInt24();

    String readIso639();

    String readString();

    long position();

    long remaining();

    String readString(int i);

    long skip(long size);

    void position(long l);

    int read(byte[] buffer);

    IsoBufferWrapper getSegment(long startOffset, long sizeIfNotParsed);

    long readUInt32();

    long readUInt64();

    byte read();

    int readUInt16();

    long size();

    byte[] read(int i);

    double readFixedPoint1616();

    float readFixedPoint88();
}
