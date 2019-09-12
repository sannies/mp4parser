package org.mp4parser.muxer;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 * Typically used for tests.
 */
public class InMemRandomAccessSourceImpl implements RandomAccessSource {
    ByteBuffer buffer;

    public InMemRandomAccessSourceImpl(ByteBuffer buffer) {
        this.buffer = buffer.duplicate();
    }

    public InMemRandomAccessSourceImpl(byte[] b) {
        buffer = ByteBuffer.wrap(b);
    }

    public synchronized ByteBuffer get(long offset, long size) throws IOException {
        ((Buffer)buffer).position(l2i(offset));
        return (ByteBuffer) ((Buffer)buffer.slice()).limit(l2i(size));
    }

    public void close() throws IOException {

    }
}
