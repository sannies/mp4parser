package com.coremedia.iso.boxes;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class ItemDataBox extends AbstractFullBox {
    ByteBuffer data;
    public static final String TYPE = "idat";


    public ItemDataBox() {
        super(TYPE);
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    @Override
    protected long getContentSize() {
        return data.limit();
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        data = content.slice();
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        bb.put(data);
    }
}
