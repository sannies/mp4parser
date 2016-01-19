package org.mp4parser.muxer.tracks.webvtt.sampleboxes;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Utf8;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static org.mp4parser.tools.CastUtils.l2i;

public abstract class AbstractCueBox implements Box {
    String content = "";
    String type;

    public AbstractCueBox(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSize() {
        return 8 + Utf8.utf8StringLengthInBytes(content);
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(l2i(getSize()));
        IsoTypeWriter.writeUInt32(header, getSize());
        header.put(IsoFile.fourCCtoBytes(getType()));
        header.put(Utf8.convert(content));
        writableByteChannel.write((ByteBuffer) header.rewind());
    }

    public String getType() {
        return type;
    }
}
