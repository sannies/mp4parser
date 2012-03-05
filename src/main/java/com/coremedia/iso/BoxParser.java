package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Basic interface to create boxes from a <code>IsoBufferWrapper</code> and its parent.
 */
public interface BoxParser {
    Class<? extends Box> getClassForFourCc(String type, byte[] userType, String parent);

    Box parseBox(ReadableByteChannel in, ContainerBox parent) throws IOException;
}
