package org.mp4parser.muxer;

import org.mp4parser.support.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class FileDataSourceImpl implements DataSource {
    private static Logger LOG = Logger.getLogger(FileDataSourceImpl.class);
    FileChannel fc;
    String filename;


    public FileDataSourceImpl(File f) throws FileNotFoundException {
        this.fc = new FileInputStream(f).getChannel();
        this.filename = f.getName();
    }

    public FileDataSourceImpl(String f) throws FileNotFoundException {
        File file = new File(f);
        this.fc = new FileInputStream(file).getChannel();
        this.filename = file.getName();
    }


    public FileDataSourceImpl(FileChannel fc) {
        this.fc = fc;
        this.filename = "unknown";
    }

    public FileDataSourceImpl(FileChannel fc, String filename) {
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
        LOG.logDebug(startPosition + " " + size);
        return fc.map(FileChannel.MapMode.READ_ONLY, startPosition, size);
    }

    public void close() throws IOException {
        fc.close();
    }

    @Override
    public String toString() {
        return filename;
    }
}
