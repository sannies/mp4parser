package com.googlecode.mp4parser.authoring.tracks.webvtt.sampleboxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.mp4parser.streaming.WriteOnlyBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public abstract class AbstractCueBox extends WriteOnlyBox {
    String content = "";

    public AbstractCueBox(String type) {
        super(type);
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


}
