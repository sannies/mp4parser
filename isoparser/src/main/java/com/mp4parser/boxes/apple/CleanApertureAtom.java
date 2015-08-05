package com.mp4parser.boxes.apple;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.support.AbstractFullBox;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * A container atom that stores information for video correction in the form of three required atoms.
 * This atom is optionally included in the track atom. The type of the track aperture mode dimensions
 * atom is 'tapt'.
 */
public class CleanApertureAtom extends AbstractFullBox {
    public static final String TYPE = "clef";

    double width;
    double height;

    public CleanApertureAtom() {
        super(TYPE);
    }


    @Override
    protected long getContentSize() {
        return 12;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, width);
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, height);
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        width = IsoTypeReader.readFixedPoint1616(content);
        height = IsoTypeReader.readFixedPoint1616(content);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
