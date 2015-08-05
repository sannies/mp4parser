package com.mp4parser.boxes.iso14496.part12;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.Utf8;
import com.mp4parser.support.AbstractFullBox;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class XmlBox extends AbstractFullBox {
    String xml = "";
    public static final String TYPE = "xml ";

    public XmlBox() {
        super(TYPE);
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    @Override
    protected long getContentSize() {
        return 4 + Utf8.utf8StringLengthInBytes(xml);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        xml = IsoTypeReader.readString(content, content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf8.convert(xml));
    }

    @Override
    public String toString() {
        return "XmlBox{" +
                "xml='" + xml + '\'' +
                '}';
    }
}
