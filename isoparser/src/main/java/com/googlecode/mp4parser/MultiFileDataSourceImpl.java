package com.googlecode.mp4parser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * A rather naive DataSource implementation allowing multiple files as source. Not as performant and memory efficient
 * as a normal FileDataSourceImpl but helpful if video packets are dumped packet by packet to disk.
 */
public class MultiFileDataSourceImpl implements DataSource {
    FileChannel[] fcs;
    int index = 0;


    public MultiFileDataSourceImpl(File... f) throws FileNotFoundException {

        this.fcs = new FileChannel[f.length];
        for (int i = 0; i < f.length; i++) {
            fcs[i] = new FileInputStream(f[i]).getChannel();
        }
    }


    public int read(ByteBuffer byteBuffer) throws IOException {
        int numOfBytesToRead = byteBuffer.remaining();
        int numOfBytesRead = 0;
        if ((numOfBytesRead = fcs[index].read(byteBuffer)) != numOfBytesToRead) {
            index++;
            return numOfBytesRead + read(byteBuffer);
        } else {
            return numOfBytesRead;
        }

    }

    public long size() throws IOException {
        long size = 0;
        for (FileChannel fileChannel : fcs) {
            size += fileChannel.size();
        }
        return size;
    }

    public long position() throws IOException {
        long position = 0;
        for (int i = 0; i < index; i++) {
            position += fcs[i].size();

        }
        return position + fcs[index].position();
    }

    public void position(long nuPos) throws IOException {
        for (int i = 0; i < fcs.length; i++) {
            if ((nuPos - fcs[i].size()) < 0) {
                fcs[i].position(nuPos);
                index = i;
                break;
            } else {
                nuPos -= fcs[i].size();
            }
        }
    }

    public long transferTo(long startPosition, long count, WritableByteChannel sink) throws IOException {
        if (count == 0) {
            return 0;
        }
        long currentPos = 0;
        for (FileChannel fc : fcs) {
            long size = fc.size();
            if (startPosition >= currentPos && startPosition < currentPos + size && startPosition + count > currentPos) { // current fcs reaches into fcs
                long bytesToTransfer = Math.min(count, size - (startPosition - currentPos));
                fc.transferTo(startPosition - currentPos, bytesToTransfer, sink);
                return bytesToTransfer + transferTo(startPosition + bytesToTransfer, count - bytesToTransfer, sink);
            }
            currentPos += size;

        }
        return 0;
    }

    public ByteBuffer map(long startPosition, long size) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(l2i(size));
        transferTo(startPosition, size, Channels.newChannel(baos));
        return ByteBuffer.wrap(baos.toByteArray());
    }


    public void close() throws IOException {
        for (FileChannel fileChannel : fcs) {
            fileChannel.close();
        }
    }

}
