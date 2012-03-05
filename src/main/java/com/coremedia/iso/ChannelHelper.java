package com.coremedia.iso;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import static com.coremedia.iso.boxes.CastUtils.l2i;


public class ChannelHelper {
    public static ByteBuffer readFully(final ReadableByteChannel channel, long size) throws IOException {

        if (channel instanceof FileChannel && size > 1024 * 1024) {
            ByteBuffer bb = ((FileChannel) channel).map(FileChannel.MapMode.READ_ONLY, ((FileChannel) channel).position(), size);
            ((FileChannel) channel).position(((FileChannel) channel).position() + size);
            return bb;
        } else {
            ByteBuffer buf = ByteBuffer.allocate(l2i(size));
            readFully(channel, buf, buf.limit());
            buf.rewind();
            assert buf.limit() == size;

            return buf;
        }

    }


    public static void readFully(final ReadableByteChannel channel, final ByteBuffer buf)
            throws IOException {
        readFully(channel, buf, buf.remaining());
    }

    public static int readFully(final ReadableByteChannel channel, final ByteBuffer buf, final int length)
            throws IOException {
        int n, count = 0;
        while (-1 != (n = channel.read(buf))) {
            count += n;
            if (count == length) {
                break;
            }
        }
        if (n == -1) {
            throw new EOFException("End of file. No more boxes.");
        }
        return count;
    }


    public static void writeFully(final WritableByteChannel channel, final ByteBuffer buf)
            throws IOException {
        do {
            int written = channel.write(buf);
            if (written < 0) {
                throw new EOFException();
            }
        } while (buf.hasRemaining());
    }


    public static void close(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException e) {
            // nop
        }

    }


}