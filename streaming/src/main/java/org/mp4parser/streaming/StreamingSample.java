package org.mp4parser.streaming;

import java.nio.ByteBuffer;

/**
 * The most simple sample has a presentation time and content.
 */
public interface StreamingSample {
    ByteBuffer getContent();

    long getDuration();

    <T extends SampleExtension> T getSampleExtension(Class<T> clazz);

    void addSampleExtension(SampleExtension sampleExtension);

    <T extends SampleExtension> T removeSampleExtension(Class<T> clazz);
}
