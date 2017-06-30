package org.mp4parser.muxer;

import org.mp4parser.boxes.sampleentry.SampleEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public interface Sample {

    void writeTo(WritableByteChannel channel) throws IOException;

    long getSize();

    ByteBuffer asByteBuffer();

    SampleEntry getSampleEntry();

}
