package com.googlecode.mp4parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 10/15/13.
 */
public class MemoryDataSourceImpl implements DataSource {
    ByteBuffer data;

    public MemoryDataSourceImpl(byte[] data) {
        this.data = ByteBuffer.wrap(data);
    }

    public MemoryDataSourceImpl(ByteBuffer buffer) {
        this.data = buffer;
    }

    public int read(ByteBuffer byteBuffer) throws IOException {
        byte[] buf = new byte[Math.min(byteBuffer.remaining(), data.remaining())];
        data.get(buf);
        byteBuffer.put(buf);
        return buf.length;
    }

    public long size() throws IOException {
        return data.capacity();
    }

    public long position() throws IOException {
        return data.position();
    }

    public void position(long nuPos) throws IOException {
        data.position(l2i(nuPos));
    }

    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        return target.write((ByteBuffer) ((ByteBuffer) data.position(l2i(position))).slice().limit(l2i(count)));
    }

    public ByteBuffer map(long startPosition, long size) throws IOException {
        return (ByteBuffer) ((ByteBuffer) data.position(l2i(startPosition))).slice().limit(l2i(size));
    }

    public void close() throws IOException {
        //nop
    }
}
