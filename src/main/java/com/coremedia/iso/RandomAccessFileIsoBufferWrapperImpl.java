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

    RandomAccessFile raf;

    public RandomAccessFileIsoBufferWrapperImpl(File file) throws IOException {

        raf = new RandomAccessFile(file, "r");


    }

    public long position() throws IOException {
        return raf.getChannel().position();
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
        return raf.length() - raf.getChannel().position();
    }

    public int read() throws IOException {
        return raf.read();
    }

    public int read(byte[] b) throws IOException {
        return raf.read(b, 0, b.length);
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
