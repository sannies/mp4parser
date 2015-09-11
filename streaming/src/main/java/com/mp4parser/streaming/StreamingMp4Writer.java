package com.mp4parser.streaming;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Very basic interface to start and stop the creation of an MP4.
 */
public interface StreamingMp4Writer extends Closeable, Callable<Void> {
    /**
     * Should free all resources blocked and interrupts the process of writing the output.
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Starts writing the MP4. This might involve starting the read process.
     * @return nothing
     * @throws IOException if writing (or reading) fails.
     */
    Void call() throws IOException;
}
