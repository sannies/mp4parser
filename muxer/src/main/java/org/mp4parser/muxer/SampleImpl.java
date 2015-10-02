package org.mp4parser.muxer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static org.mp4parser.tools.CastUtils.l2i;

public class SampleImpl implements Sample {
    private final long offset;
    private final long size;
    private ByteBuffer[] data;

    public SampleImpl(ByteBuffer buf) {
        this.offset = -1;
        this.size = buf.limit();
        this.data = new ByteBuffer[]{buf};
    }

    public SampleImpl(ByteBuffer[] data) {
        this.offset = -1;
        int _size = 0;
        for (ByteBuffer byteBuffer : data) {
            _size += byteBuffer.remaining();
        }
        this.size = _size;
        this.data = data;
    }

    public SampleImpl(long offset, long sampleSize, ByteBuffer data) {
        this.offset = offset;
        this.size = sampleSize;
        this.data = new ByteBuffer[]{data};
    }

    public void writeTo(WritableByteChannel channel) throws IOException {
        for (ByteBuffer b : data) {
            channel.write(b.duplicate());
        }
    }

    public long getSize() {
        return size;
    }

    public ByteBuffer asByteBuffer() {
        byte[] bCopy = new byte[l2i(size)];
        ByteBuffer copy = ByteBuffer.wrap(bCopy);
        for (ByteBuffer b : data) {
            copy.put(b.duplicate());
        }
        copy.rewind();
        return copy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SampleImpl");
        sb.append("{offset=").append(offset);
        sb.append("{size=").append(size);
        sb.append('}');
        return sb.toString();
    }
}
