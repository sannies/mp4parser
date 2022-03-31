package org.mp4parser.boxes.webm;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;

public class ContentLightLevelBox extends AbstractFullBox {
    public static final String TYPE = "CoLL";

    private int maxCLL;
    private int maxFALL;

    protected ContentLightLevelBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 8;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, maxCLL);
        IsoTypeWriter.writeUInt16(byteBuffer, maxFALL);
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        maxCLL = IsoTypeReader.readUInt16(content);
        maxFALL = IsoTypeReader.readUInt16(content);
    }

    public int getMaxCLL() {
        return maxCLL;
    }

    public void setMaxCLL(int maxCLL) {
        this.maxCLL = maxCLL;
    }

    public int getMaxFALL() {
        return maxFALL;
    }

    public void setMaxFALL(int maxFALL) {
        this.maxFALL = maxFALL;
    }
}
