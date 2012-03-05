package com.googlecode.mp4parser;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Creates a <code>ReadableByteChannel</code> that is backed by a <code>ByteBuffer</code>.
 */
public class ByteBufferByteChannel implements ByteChannel {
    ByteBuffer byteBuffer;

    public ByteBufferByteChannel(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public int read(ByteBuffer dst) throws IOException {
        byte[] b = dst.array();
        int r = dst.remaining();
        if (byteBuffer.remaining() >= r) {
            byteBuffer.get(b, dst.position(), r);
            return r;
        } else {
            throw new EOFException("Reading beyond end of stream");
        }
    }

    public boolean isOpen() {
        return true;
    }

    public void close() throws IOException {
    }

    public int write(ByteBuffer src) throws IOException {
        int r = src.remaining();
        byteBuffer.put(src);
        return r;
    }
}
