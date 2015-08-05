package com.googlecode.mp4parser;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.boxes.UserBox;
import com.mp4parser.tools.UUIDConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by sannies on 03.09.2014.
 */
public class MyCustomStringBox extends UserBox {
    public MyCustomStringBox() {
        super(UUIDConverter.convert(UUID.fromString("550e8400-e29b-11d4-a716-446655440000")));
    }
    long a, b, c, d;

    @Override
    public void _parseDetails(ByteBuffer content) {
        a = IsoTypeReader.readUInt32(content);
        b = IsoTypeReader.readUInt32(content);
        c = IsoTypeReader.readUInt32(content);
        d = IsoTypeReader.readUInt32(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt32(byteBuffer, a);
        IsoTypeWriter.writeUInt32(byteBuffer, b);
        IsoTypeWriter.writeUInt32(byteBuffer, c);
        IsoTypeWriter.writeUInt32(byteBuffer, d);
    }

    @Override
    protected long getContentSize() {
        return 16;
    }
}
