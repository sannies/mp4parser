package com.googlecode.mp4parser.h264;

import com.coremedia.iso.IsoBufferWrapper;

import java.io.IOException;

/**
 * @author Stanislav Vitvitskiy
 */
public interface AccessUnit {
    IsoBufferWrapper nextNALUnit() throws IOException;
}
