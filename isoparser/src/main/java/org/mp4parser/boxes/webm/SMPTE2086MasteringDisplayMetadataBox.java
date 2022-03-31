package org.mp4parser.boxes.webm;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;

public class SMPTE2086MasteringDisplayMetadataBox extends AbstractFullBox {
    private static final String TYPE = "SmDm";

    int primaryRChromaticity_x;
    int primaryRChromaticity_y;
    int primaryGChromaticity_x;
    int primaryGChromaticity_y;
    int primaryBChromaticity_x;
    int primaryBChromaticity_y;
    int whitePointChromaticity_x;
    int whitePointChromaticity_y;
    long luminanceMax;
    long luminanceMin;

    public SMPTE2086MasteringDisplayMetadataBox() {
        super(TYPE);
    }

    public int getPrimaryRChromaticity_x() {
        return primaryRChromaticity_x;
    }

    public void setPrimaryRChromaticity_x(int primaryRChromaticity_x) {
        this.primaryRChromaticity_x = primaryRChromaticity_x;
    }

    public int getPrimaryRChromaticity_y() {
        return primaryRChromaticity_y;
    }

    public void setPrimaryRChromaticity_y(int primaryRChromaticity_y) {
        this.primaryRChromaticity_y = primaryRChromaticity_y;
    }

    public int getPrimaryGChromaticity_x() {
        return primaryGChromaticity_x;
    }

    public void setPrimaryGChromaticity_x(int primaryGChromaticity_x) {
        this.primaryGChromaticity_x = primaryGChromaticity_x;
    }

    public int getPrimaryGChromaticity_y() {
        return primaryGChromaticity_y;
    }

    public void setPrimaryGChromaticity_y(int primaryGChromaticity_y) {
        this.primaryGChromaticity_y = primaryGChromaticity_y;
    }

    public int getPrimaryBChromaticity_x() {
        return primaryBChromaticity_x;
    }

    public void setPrimaryBChromaticity_x(int primaryBChromaticity_x) {
        this.primaryBChromaticity_x = primaryBChromaticity_x;
    }

    public int getPrimaryBChromaticity_y() {
        return primaryBChromaticity_y;
    }

    public void setPrimaryBChromaticity_y(int primaryBChromaticity_y) {
        this.primaryBChromaticity_y = primaryBChromaticity_y;
    }

    public int getWhitePointChromaticity_x() {
        return whitePointChromaticity_x;
    }

    public void setWhitePointChromaticity_x(int whitePointChromaticity_x) {
        this.whitePointChromaticity_x = whitePointChromaticity_x;
    }

    public int getWhitePointChromaticity_y() {
        return whitePointChromaticity_y;
    }

    public void setWhitePointChromaticity_y(int whitePointChromaticity_y) {
        this.whitePointChromaticity_y = whitePointChromaticity_y;
    }

    public long getLuminanceMax() {
        return luminanceMax;
    }

    public void setLuminanceMax(long luminanceMax) {
        this.luminanceMax = luminanceMax;
    }

    public long getLuminanceMin() {
        return luminanceMin;
    }

    public void setLuminanceMin(long luminanceMin) {
        this.luminanceMin = luminanceMin;
    }

    @Override
    protected long getContentSize() {
        return 28;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, primaryRChromaticity_x);
        IsoTypeWriter.writeUInt16(byteBuffer, primaryRChromaticity_y);
        IsoTypeWriter.writeUInt16(byteBuffer, primaryGChromaticity_x);
        IsoTypeWriter.writeUInt16(byteBuffer, primaryGChromaticity_y);
        IsoTypeWriter.writeUInt16(byteBuffer, primaryBChromaticity_x);
        IsoTypeWriter.writeUInt16(byteBuffer, primaryBChromaticity_y);
        IsoTypeWriter.writeUInt16(byteBuffer, whitePointChromaticity_x);
        IsoTypeWriter.writeUInt16(byteBuffer, whitePointChromaticity_y);
        IsoTypeWriter.writeUInt32(byteBuffer, luminanceMax);
        IsoTypeWriter.writeUInt32(byteBuffer, luminanceMin);
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        primaryRChromaticity_x = IsoTypeReader.readUInt16(content);
        primaryRChromaticity_y = IsoTypeReader.readUInt16(content);
        primaryGChromaticity_x = IsoTypeReader.readUInt16(content);
        primaryGChromaticity_y = IsoTypeReader.readUInt16(content);
        primaryBChromaticity_x = IsoTypeReader.readUInt16(content);
        primaryBChromaticity_y = IsoTypeReader.readUInt16(content);
        whitePointChromaticity_x = IsoTypeReader.readUInt16(content);
        whitePointChromaticity_y = IsoTypeReader.readUInt16(content);
        luminanceMax = IsoTypeReader.readUInt32(content);
        luminanceMin = IsoTypeReader.readUInt32(content);
    }
}
