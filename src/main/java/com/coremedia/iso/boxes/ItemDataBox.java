package com.coremedia.iso.boxes;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class ItemDataBox extends AbstractBox {
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
        data = content.slice();
        content.position(content.position() + content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        bb.put(data);
    }
}
