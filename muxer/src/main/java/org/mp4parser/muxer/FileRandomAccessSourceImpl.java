package org.mp4parser.muxer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class FileRandomAccessSourceImpl implements RandomAccessSource {
    private RandomAccessFile raf;

    public FileRandomAccessSourceImpl(RandomAccessFile raf) {
        this.raf = raf;
    }

    public ByteBuffer get(long offset, long size) throws IOException {
        return raf.getChannel().map(FileChannel.MapMode.READ_ONLY, offset, size);
    }

    public void close() throws IOException {
        raf.close();
    }
}
