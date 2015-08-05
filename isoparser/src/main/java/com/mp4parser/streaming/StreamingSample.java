package com.mp4parser.streaming;

import java.nio.ByteBuffer;

/**
 * The most simple sample has a presentation time and content.
 */
public interface StreamingSample {
    ByteBuffer getContent();
    long getDuration();
    SampleExtension[] getExtensions();
}
