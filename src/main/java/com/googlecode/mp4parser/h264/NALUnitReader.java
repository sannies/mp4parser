package com.googlecode.mp4parser.h264;

import com.coremedia.iso.IsoBufferWrapper;

import java.io.IOException;

public interface NALUnitReader {
    IsoBufferWrapper nextNALUnit() throws IOException;
}