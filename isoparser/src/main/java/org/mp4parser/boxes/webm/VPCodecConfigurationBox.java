package org.mp4parser.boxes.webm;


import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitWriterBuffer;
import org.mp4parser.support.AbstractFullBox;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class VPCodecConfigurationBox extends AbstractFullBox {
    public static final String TYPE = "vpcC";

    private int profile;
    private int level;
    private int bitDepth;
    private int chromaSubsampling;
    private int videoFullRangeFlag;
    private int colourPrimaries;
    private int transferCharacteristics;
    private int matrixCoefficients;

    private byte[] codecIntializationData;


    public VPCodecConfigurationBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return codecIntializationData.length + 12;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        BitWriterBuffer bwb = new BitWriterBuffer(byteBuffer);
        bwb.writeBits(profile, 8);
        bwb.writeBits(level, 8);
        bwb.writeBits(bitDepth, 4);
        bwb.writeBits(chromaSubsampling, 3);
        bwb.writeBits(videoFullRangeFlag, 1);
        bwb.writeBits(colourPrimaries, 8);
        bwb.writeBits(transferCharacteristics, 8);
        bwb.writeBits(matrixCoefficients, 8);
        bwb.writeBits(codecIntializationData.length, 16);
        byteBuffer.put(codecIntializationData);
    }

    // aligned (8) class VPCodecConfigurationRecord {
    //    unsigned int (8)     profile;
    //    unsigned int (8)     level;
    //    unsigned int (4)     bitDepth;
    //    unsigned int (3)     chromaSubsampling;
    //    unsigned int (1)     videoFullRangeFlag;
    //    unsigned int (8)     colourPrimaries;
    //    unsigned int (8)     transferCharacteristics;
    //    unsigned int (8)     matrixCoefficients;
    //    unsigned int (16)    codecIntializationDataSize;
    //    unsigned int (8)[]   codecIntializationData;
    //}

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        BitReaderBuffer brb = new BitReaderBuffer(content);
        profile = brb.readBits(8);
        level = brb.readBits(8);
        bitDepth = brb.readBits(4);
        chromaSubsampling = brb.readBits(3);
        videoFullRangeFlag = brb.readBits(1);
        colourPrimaries = brb.readBits(8);
        transferCharacteristics = brb.readBits(8);
        matrixCoefficients = brb.readBits(8);
        int len = brb.readBits(16);
        codecIntializationData = new byte[len];
        content.get(codecIntializationData);
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }

    public int getChromaSubsampling() {
        return chromaSubsampling;
    }

    public void setChromaSubsampling(int chromaSubsampling) {
        this.chromaSubsampling = chromaSubsampling;
    }

    public int getVideoFullRangeFlag() {
        return videoFullRangeFlag;
    }

    public void setVideoFullRangeFlag(int videoFullRangeFlag) {
        this.videoFullRangeFlag = videoFullRangeFlag;
    }

    public int getColourPrimaries() {
        return colourPrimaries;
    }

    public void setColourPrimaries(int colourPrimaries) {
        this.colourPrimaries = colourPrimaries;
    }

    public int getTransferCharacteristics() {
        return transferCharacteristics;
    }

    public void setTransferCharacteristics(int transferCharacteristics) {
        this.transferCharacteristics = transferCharacteristics;
    }

    public int getMatrixCoefficients() {
        return matrixCoefficients;
    }

    public void setMatrixCoefficients(int matrixCoefficients) {
        this.matrixCoefficients = matrixCoefficients;
    }

    public byte[] getCodecIntializationData() {
        return codecIntializationData;
    }

    public void setCodecIntializationData(byte[] codecIntializationData) {
        this.codecIntializationData = codecIntializationData;
    }

    @Override
    public String toString() {
        return "VPCodecConfigurationBox{" +
                "profile=" + profile +
                ", level=" + level +
                ", bitDepth=" + bitDepth +
                ", chromaSubsampling=" + chromaSubsampling +
                ", videoFullRangeFlag=" + videoFullRangeFlag +
                ", colourPrimaries=" + colourPrimaries +
                ", transferCharacteristics=" + transferCharacteristics +
                ", matrixCoefficients=" + matrixCoefficients +
                ", codecIntializationData=" + Arrays.toString(codecIntializationData) +
                '}';
    }
}