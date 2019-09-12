package org.mp4parser.boxes.sampleentry;

import org.mp4parser.BoxParser;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class DfxpSampleEntry extends AbstractSampleEntry {
    public DfxpSampleEntry() {
        super("dfxp");
    }

    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(8);
        dataSource.read(content);
        ((Buffer)content).position(6);
        dataReferenceIndex = IsoTypeReader.readUInt16(content);
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        ((Buffer)byteBuffer).position(6);
        IsoTypeWriter.writeUInt16(byteBuffer, dataReferenceIndex);
        writableByteChannel.write(byteBuffer);
    }

    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 8;
        return s + t + ((largeBox || (s + t + 8) >= (1L << 32)) ? 16 : 8);
    }
}
