package com.mp4parser.muxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Allows random access to some data source as some data structure such as chunks in an mdat
 * require random access with absolute offsets.
 */
public interface RandomAccessSource {
    ByteBuffer get(long offset, long size) throws IOException;
}
