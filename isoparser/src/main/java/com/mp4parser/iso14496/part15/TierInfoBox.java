package com.mp4parser.iso14496.part15;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractBox;

import java.nio.ByteBuffer;

public class TierInfoBox extends AbstractBox {
    public static final String TYPE = "tiri";

    int tierID;
    int profileIndication;
    int profile_compatibility;
    int levelIndication;
    int reserved1 = 0;
    int visualWidth;
    int visualHeight;
    int discardable;
    int constantFrameRate;
    int reserved2 = 0;
    int frameRate;

    public TierInfoBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 13;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt16(byteBuffer, tierID);

        IsoTypeWriter.writeUInt8(byteBuffer, profileIndication);
        IsoTypeWriter.writeUInt8(byteBuffer, profile_compatibility);
        IsoTypeWriter.writeUInt8(byteBuffer, levelIndication);
        IsoTypeWriter.writeUInt8(byteBuffer, reserved1);

        IsoTypeWriter.writeUInt16(byteBuffer, visualWidth);
        IsoTypeWriter.writeUInt16(byteBuffer, visualHeight);

        IsoTypeWriter.writeUInt8(byteBuffer, (discardable << 6) + (constantFrameRate << 4) +reserved2);

        IsoTypeWriter.writeUInt16(byteBuffer, frameRate);
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        tierID = IsoTypeReader.readUInt16(content);

        profileIndication = IsoTypeReader.readUInt8(content);
        profile_compatibility = IsoTypeReader.readUInt8(content);
        levelIndication = IsoTypeReader.readUInt8(content);
        reserved1 = IsoTypeReader.readUInt8(content);

        visualWidth = IsoTypeReader.readUInt16(content);
        visualHeight = IsoTypeReader.readUInt16(content);
        int a = IsoTypeReader.readUInt8(content);
        discardable = (a & 0xC0) >> 6;
        constantFrameRate = (a & 0x30) >> 4;
        reserved2 = a & 0xf;
        frameRate = IsoTypeReader.readUInt16(content);

    }

    public int getTierID() {
        return tierID;
    }

    public void setTierID(int tierID) {
        this.tierID = tierID;
    }

    public int getProfileIndication() {
        return profileIndication;
    }

    public void setProfileIndication(int profileIndication) {
        this.profileIndication = profileIndication;
    }

    public int getProfile_compatibility() {
        return profile_compatibility;
    }

    public void setProfile_compatibility(int profile_compatibility) {
        this.profile_compatibility = profile_compatibility;
    }

    public int getLevelIndication() {
        return levelIndication;
    }

    public void setLevelIndication(int levelIndication) {
        this.levelIndication = levelIndication;
    }

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        this.reserved1 = reserved1;
    }

    public int getVisualWidth() {
        return visualWidth;
    }

    public void setVisualWidth(int visualWidth) {
        this.visualWidth = visualWidth;
    }

    public int getVisualHeight() {
        return visualHeight;
    }

    public void setVisualHeight(int visualHeight) {
        this.visualHeight = visualHeight;
    }

    public int getDiscardable() {
        return discardable;
    }

    public void setDiscardable(int discardable) {
        this.discardable = discardable;
    }

    public int getConstantFrameRate() {
        return constantFrameRate;
    }

    public void setConstantFrameRate(int constantFrameRate) {
        this.constantFrameRate = constantFrameRate;
    }

    public int getReserved2() {
        return reserved2;
    }

    public void setReserved2(int reserved2) {
        this.reserved2 = reserved2;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }
}