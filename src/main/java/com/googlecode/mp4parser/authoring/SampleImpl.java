package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.boxes.Container;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class SampleImpl implements Sample {
    private final long offset;
    private final long size;
    private ByteBuffer[] data;
    private final Container parent;

    public SampleImpl(ByteBuffer buf) {
        this.offset = -1;
        this.size = buf.limit();
        this.data = new ByteBuffer[]{buf};
        this.parent = null;
    }

    public SampleImpl(ByteBuffer[] data) {
        this.offset = -1;
        int _size = 0;
        for (ByteBuffer byteBuffer : data) {
            _size += byteBuffer.remaining();
        }
        this.size = _size;
        this.data = data;
        this.parent = null;
    }

    public SampleImpl(long offset, long sampleSize, ByteBuffer data) {
        this.offset = offset;
        this.size = sampleSize;
        this.data = new ByteBuffer[]{data};
        this.parent = null;
    }

    public SampleImpl(long offset, long sampleSize, Container parent) {
        this.offset = offset;
        this.size = sampleSize;
        this.data = null;
        this.parent = parent;
    }

    protected void ensureData() {
        if (data != null) return;
        if (parent == null) {
            throw new RuntimeException("Missing parent container, can't read sample " + this);
        }
        try {
            data = new ByteBuffer[]{parent.getByteBuffer(offset, size)};
        } catch (IOException e) {
            throw new RuntimeException("couldn't read sample " + this, e);
        }
    }

    public void writeTo(WritableByteChannel channel) throws IOException {
        ensureData();
        for (ByteBuffer b : data) {
            channel.write(b.duplicate());
        }
    }

    public long getSize() {
        return size;
    }

    public ByteBuffer asByteBuffer() {
        ensureData();
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
