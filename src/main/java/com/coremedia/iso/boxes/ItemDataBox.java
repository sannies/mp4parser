package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 *
 */
public class ItemDataBox extends AbstractBox {
    byte[] data;
    public static final String TYPE = "idat";

    public ItemDataBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    protected long getContentSize() {
        return data.length;
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(data);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        assert size < Integer.MAX_VALUE;
        data = new byte[(int) size];
        in.read(data);
    }
}
