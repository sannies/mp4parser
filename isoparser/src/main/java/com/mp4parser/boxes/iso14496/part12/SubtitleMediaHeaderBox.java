package com.mp4parser.boxes.iso14496.part12;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class SubtitleMediaHeaderBox extends AbstractMediaHeaderBox {

    public static final String TYPE = "sthd";

    public SubtitleMediaHeaderBox() {
        super(TYPE);
    }

    protected long getContentSize() {
        return 4;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
    }

    public String toString() {
        return "SubtitleMediaHeaderBox";
    }
}
