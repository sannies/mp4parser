package com.mp4parser.streaming;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 */
public interface StreamingMp4Writer extends Closeable {
    void close() throws IOException;
    void write() throws IOException;
}
