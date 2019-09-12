package org.mp4parser.muxer.tracks.webvtt.sampleboxes;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class VTTEmptyCueBox implements Box {
    public VTTEmptyCueBox() {
    }

    public long getSize() {
        return 8;
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt32(header, getSize());
        header.put(IsoFile.fourCCtoBytes(getType()));
        writableByteChannel.write((ByteBuffer)((Buffer) header).rewind());
    }

    public String getType() {
        return "vtte";
    }
}
