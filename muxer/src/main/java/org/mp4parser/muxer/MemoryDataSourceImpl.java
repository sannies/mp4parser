package org.mp4parser.muxer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static org.mp4parser.tools.CastUtils.l2i;

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
        if (0 == data.remaining() && 0 != byteBuffer.remaining()) {
            return -1;
        }
        int size = Math.min(byteBuffer.remaining(), data.remaining());
        if (byteBuffer.hasArray()) {
            byteBuffer.put(data.array(), data.position(), size);
            data.position(data.position() + size);
        } else {
            byte[] buf = new byte[size];
            data.get(buf);
            byteBuffer.put(buf);
        }
        return size;
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
        int oldPosition = data.position();
        data.position(l2i(startPosition));
        ByteBuffer result = data.slice();
        result.limit(l2i(size));
        data.position(oldPosition);
        return result;
    }

    public void close() throws IOException {
        //nop
    }

}
