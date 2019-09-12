package org.mp4parser.muxer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mp4parser.tools.CastUtils.l2i;


public class FileDataSourceViaHeapImpl implements DataSource {
    private static Logger LOG = LoggerFactory.getLogger(FileDataSourceViaHeapImpl.class);
    FileChannel fc;
    String filename;


    public FileDataSourceViaHeapImpl(File f) throws FileNotFoundException {
        this.fc = new FileInputStream(f).getChannel();
        this.filename = f.getName();
    }

    public FileDataSourceViaHeapImpl(String f) throws FileNotFoundException {
        File file = new File(f);
        this.fc = new FileInputStream(file).getChannel();
        this.filename = file.getName();
    }


    public FileDataSourceViaHeapImpl(FileChannel fc) {
        this.fc = fc;
        this.filename = "unknown";
    }

    public FileDataSourceViaHeapImpl(FileChannel fc, String filename) {
        this.fc = fc;
        this.filename = filename;
    }

    public synchronized int read(ByteBuffer byteBuffer) throws IOException {
        return fc.read(byteBuffer);
    }

    public synchronized long size() throws IOException {
        return fc.size();
    }

    public synchronized long position() throws IOException {
        return fc.position();
    }

    public synchronized void position(long nuPos) throws IOException {
        fc.position(nuPos);
    }

    public synchronized long transferTo(long startPosition, long count, WritableByteChannel sink) throws IOException {
        return fc.transferTo(startPosition, count, sink);
    }

    public synchronized ByteBuffer map(long startPosition, long size) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(l2i(size));
        fc.read(bb, startPosition);
        return (ByteBuffer) ((Buffer)bb).rewind();
    }

    public void close() throws IOException {
        fc.close();
    }

    @Override
    public String toString() {
        return filename;
    }

}
