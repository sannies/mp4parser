package com.coremedia.iso.boxes;

import java.io.IOException;
import java.nio.ByteBuffer;

public class GenericMediaHeaderBoxImpl extends AbstractMediaHeaderBox {

    ByteBuffer data;

    @Override
    protected long getContentSize() {
        return 4 + data.limit();
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.data = content.slice();
        content.position(content.remaining() + content.position());

    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        bb.put((ByteBuffer) data.rewind());
    }

    public GenericMediaHeaderBoxImpl() {
        super("gmhd");
    }
}
