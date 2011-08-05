package com.coremedia.iso;

import java.io.IOException;

/**
 * Encapsulates access to underlying storage.
 */
public interface IsoBufferWrapper {
    int readUInt8() throws IOException;

    int readUInt24() throws IOException;

    String readIso639() throws IOException;

    String readString() throws IOException;

    long position() throws IOException;

    long remaining() throws IOException;

    String readString(int i) throws IOException;

    long skip(long size) throws IOException;

    void position(long l) throws IOException;

    int read(byte[] buffer) throws IOException;

    IsoBufferWrapper getSegment(long startOffset, long length) throws IOException;

    long readUInt32() throws IOException;

    long readUInt64() throws IOException;

    byte read() throws IOException;

    int readUInt16() throws IOException;

    long size();

    byte[] read(int i) throws IOException;

    double readFixedPoint1616() throws IOException;

    float readFixedPoint88() throws IOException;

    int readUInt16BE() throws IOException;

    long readUInt32BE() throws IOException;
}
