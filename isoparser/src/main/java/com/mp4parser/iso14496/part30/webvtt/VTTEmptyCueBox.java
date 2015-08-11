package com.mp4parser.iso14496.part30.webvtt;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.mp4parser.streaming.WriteOnlyBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Created by sannies on 11.08.2015.
 */
public class VTTEmptyCueBox extends WriteOnlyBox {
    public VTTEmptyCueBox() {
        super("vtte");
    }

    public long getSize() {
        return 8;
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt32(header, getSize());
        header.put(IsoFile.fourCCtoBytes(getType()));
        writableByteChannel.write((ByteBuffer) header.rewind());
    }
}
