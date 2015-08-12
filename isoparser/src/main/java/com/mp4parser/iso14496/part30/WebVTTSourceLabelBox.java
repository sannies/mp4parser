package com.mp4parser.iso14496.part30;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractBox;

import java.nio.ByteBuffer;

/**
 * Created by sannies on 04.12.2014.
 */
public class WebVTTSourceLabelBox extends AbstractBox {
    public static final String TYPE = "vlab";


    String sourceLabel = "";

    public WebVTTSourceLabelBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return Utf8.utf8StringLengthInBytes(sourceLabel);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(Utf8.convert(sourceLabel));
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        sourceLabel = IsoTypeReader.readString(content, content.remaining());
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }
}
