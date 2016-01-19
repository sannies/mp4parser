package org.mp4parser.muxer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Allows random access to some data source as some data structure such as chunks in an mdat
 * require random access with absolute offsets.
 */
public interface RandomAccessSource extends Closeable {
    ByteBuffer get(long offset, long size) throws IOException;
}
