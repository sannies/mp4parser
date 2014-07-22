package com.googlecode.mp4parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by sannies on 29.08.13.
 */
public class FileDataSourceImpl implements DataSource {
    FileChannel fc;


    public FileDataSourceImpl(File f) throws FileNotFoundException {
        this.fc = new FileInputStream(f).getChannel();
    }

    public FileDataSourceImpl(String f) throws FileNotFoundException {
        this.fc = new FileInputStream(new File(f)).getChannel();
    }

    public FileDataSourceImpl(FileChannel fc) {
        this.fc = fc;
    }

    public int read(ByteBuffer byteBuffer) throws IOException {
        return fc.read(byteBuffer);
    }

    public long size() throws IOException {
        return fc.size();
    }

    public long position() throws IOException {
        return fc.position();
    }

    public void position(long nuPos) throws IOException {
        fc.position(nuPos);
    }

    public long transferTo(long startPosition, long count, WritableByteChannel sink) throws IOException {
        return fc.transferTo(startPosition, count, sink);
    }

    public ByteBuffer map(long startPosition, long size) throws IOException {
        return fc.map(FileChannel.MapMode.READ_ONLY, startPosition, size);
    }

    public void close() throws IOException {
        fc.close();
    }
}
