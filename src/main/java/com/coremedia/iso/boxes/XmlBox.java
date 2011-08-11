package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 *
 */
public class XmlBox extends AbstractFullBox {
    String xml;

    public XmlBox() {
        super(IsoFile.fourCCtoBytes("xml "));
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    @Override
    protected long getContentSize() {
        return IsoFile.utf8StringLengthInBytes(xml);
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeStringNoTerm(xml);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        long a = in.remaining();
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long b = in.remaining();
        size -= (a - b);
        assert size < Integer.MAX_VALUE;
        xml = in.readString((int) size);
    }
}
