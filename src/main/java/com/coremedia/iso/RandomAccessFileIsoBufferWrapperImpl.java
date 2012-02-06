package com.coremedia.iso;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 */
public class RandomAccessFileIsoBufferWrapperImpl extends AbstractIsoBufferWrapper {

    BufferedRandomAccessFile raf;

    public RandomAccessFileIsoBufferWrapperImpl(File file) throws IOException {

        raf = new BufferedRandomAccessFile(new RandomAccessFile(file, "r"));
        //raf = new RandomAccessFile(file, "r");


    }

    @Override
    public long readUInt32() throws IOException {
        byte[] c = new byte[4];
        raf.readFully(c);
        long uint32 = 0;
        uint32 += (c[0] < 0 ? c[0] + 256 : c[0]) << 24;
        uint32 += (c[1] < 0 ? c[1] + 256 : c[1]) << 16;
        uint32 += (c[2] < 0 ? c[2] + 256 : c[2]) << 8;
        uint32 += (c[3] < 0 ? c[3] + 256 : c[3]);
        return uint32;
    }

    @Override
    public int readUInt16() throws IOException {
        byte[] c = new byte[2];
        raf.readFully(c);
        int uint16 = 0;
        uint16 += (c[0] < 0 ? c[0] + 256 : c[0]) << 8;
        uint16 += (c[1] < 0 ? c[1] + 256 : c[1]);
        return uint16;
    }

    public long position() throws IOException {
        return raf.getFilePointer();
    }

    public void position(long position) throws IOException {
        raf.seek(position);
    }

    public long size() {
        try {
            return raf.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] read(int byteCount) throws IOException {
        byte[] result = new byte[byteCount];
        this.read(result);
        return result;

    }

    public long remaining() throws IOException {
        return raf.length() - raf.getFilePointer();
    }

    public int read() throws IOException {
        return raf.read();
    }

    public int read(byte[] b) throws IOException {
        raf.readFully(b, 0, b.length);
        return b.length;
    }

    public long skip(long n) throws IOException {
        this.position(this.position() + n);
        return n;
    }

    public IsoBufferWrapper getSegment(long startPos, long length) throws IOException {
        assert length < Integer.MAX_VALUE;
        return new IsoBufferWrapperImpl(raf.getChannel().map(FileChannel.MapMode.READ_ONLY, startPos, length));
    }

}
