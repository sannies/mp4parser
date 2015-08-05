package com.mp4parser;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * The most basic imaginable box. It does not have any parsing functionality it can be used to create boxes
 * programmatically.
 */
public interface Box {

    /**
     * The box's 4-cc type.
     *
     * @return the 4 character type of the box
     */
    String getType();

    long getSize();

    /**
     * Writes the complete box - size | 4-cc | content - to the given <code>writableByteChannel</code>.
     *
     * @param writableByteChannel the box's sink
     * @throws IOException in case of problems with the <code>Channel</code>
     */
    void getBox(WritableByteChannel writableByteChannel) throws IOException;
}
