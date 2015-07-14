package com.mp4parser.iso14496.part12;


import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.sampleentry.AbstractSampleEntry;
import com.googlecode.mp4parser.DataSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class HintSampleEntry extends AbstractSampleEntry {
    protected byte[] data;

    public HintSampleEntry(String type) {
        super(type);
    }

    @Override
    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer b1 = ByteBuffer.allocate(8);
        dataSource.read(b1);
        b1.position(6);
        dataReferenceIndex = IsoTypeReader.readUInt16(b1);
        data = new byte[l2i(contentSize - 8)];
        dataSource.read(ByteBuffer.wrap(data));
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());

        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.position(6);
        IsoTypeWriter.writeUInt16(byteBuffer, dataReferenceIndex);
        byteBuffer.rewind();
        writableByteChannel.write(byteBuffer);
        writableByteChannel.write(ByteBuffer.wrap(data));
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }



    @Override
    public long getSize() {
        long s = 8 + data.length;
        return s + ((largeBox || (s + 8) >= (1L << 32)) ? 16 : 8);
    }
}
