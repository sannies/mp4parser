package org.mp4parser.streaming;

import java.io.Closeable;
import java.io.IOException;

/**
 * Very basic interface to start and stop the creation of an MP4.
 */
public interface SampleSink extends Closeable {
    /**
     * Should free all resources blocked and interrupts the process of writing the output.
     *
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Adds a samples to the SampleSink. This might or might not cause writing the sample any output stream or channel.
     * Once this method is called the <code>StreamingTrack</code> must be ready and accept calls to any method.
     *
     * @return nothing
     * @throws IOException if writing (or reading) fails.
     */
    void acceptSample(StreamingSample streamingSample, StreamingTrack streamingTrack) throws IOException;
}
