package com.mp4parser.boxes.dolby;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.support.AbstractBox;
import com.mp4parser.support.DoNotParseDetail;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.BitWriterBuffer;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class DTSSpecificBox extends AbstractBox {

    public static final String TYPE = "ddts";

    long DTSSamplingFrequency;
    long maxBitRate;
    long avgBitRate;
    int pcmSampleDepth;
    int frameDuration;
    int streamConstruction;
    int coreLFEPresent;
    int coreLayout;
    int coreSize;
    int stereoDownmix;
    int representationType;
    int channelLayout;
    int multiAssetFlag;
    int LBRDurationMod;
    int reservedBoxPresent;
    int reserved;

    public DTSSpecificBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 20;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        DTSSamplingFrequency = IsoTypeReader.readUInt32(content);
        maxBitRate = IsoTypeReader.readUInt32(content);
        avgBitRate = IsoTypeReader.readUInt32(content);
        pcmSampleDepth = IsoTypeReader.readUInt8(content);
        BitReaderBuffer brb = new BitReaderBuffer(content);
        frameDuration = brb.readBits(2);
        streamConstruction = brb.readBits(5);
        coreLFEPresent = brb.readBits(1);
        coreLayout = brb.readBits(6);
        coreSize = brb.readBits(14);
        stereoDownmix = brb.readBits(1);
        representationType = brb.readBits(3);
        channelLayout = brb.readBits(16);
        multiAssetFlag = brb.readBits(1);
        LBRDurationMod = brb.readBits(1);
        reservedBoxPresent = brb.readBits(1);
        reserved = brb.readBits(5);

    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt32(byteBuffer, DTSSamplingFrequency);
        IsoTypeWriter.writeUInt32(byteBuffer, maxBitRate);
        IsoTypeWriter.writeUInt32(byteBuffer, avgBitRate);
        IsoTypeWriter.writeUInt8(byteBuffer, pcmSampleDepth);
        BitWriterBuffer bwb = new BitWriterBuffer(byteBuffer);
        bwb.writeBits(frameDuration, 2);
        bwb.writeBits(streamConstruction, 5);
        bwb.writeBits(coreLFEPresent, 1);
        bwb.writeBits(coreLayout, 6);
        bwb.writeBits(coreSize, 14);
        bwb.writeBits(stereoDownmix, 1);
        bwb.writeBits(representationType, 3);
        bwb.writeBits(channelLayout, 16);
        bwb.writeBits(multiAssetFlag, 1);
        bwb.writeBits(LBRDurationMod, 1);
        bwb.writeBits(reservedBoxPresent, 1);
        bwb.writeBits(reserved, 5);

    }

    public long getAvgBitRate() {
        return avgBitRate;
    }

    public void setAvgBitRate(long avgBitRate) {
        this.avgBitRate = avgBitRate;
    }

    public long getDTSSamplingFrequency() {
        return DTSSamplingFrequency;
    }

    public void setDTSSamplingFrequency(long DTSSamplingFrequency) {
        this.DTSSamplingFrequency = DTSSamplingFrequency;
    }

    public long getMaxBitRate() {
        return maxBitRate;
    }

    public void setMaxBitRate(long maxBitRate) {
        this.maxBitRate = maxBitRate;
    }

    public int getPcmSampleDepth() {
        return pcmSampleDepth;
    }

    public void setPcmSampleDepth(int pcmSampleDepth) {
        this.pcmSampleDepth = pcmSampleDepth;
    }

    public int getFrameDuration() {
        return frameDuration;
    }

    public void setFrameDuration(int frameDuration) {
        this.frameDuration = frameDuration;
    }

    public int getStreamConstruction() {
        return streamConstruction;
    }

    public void setStreamConstruction(int streamConstruction) {
        this.streamConstruction = streamConstruction;
    }

    public int getCoreLFEPresent() {
        return coreLFEPresent;
    }

    public void setCoreLFEPresent(int coreLFEPresent) {
        this.coreLFEPresent = coreLFEPresent;
    }

    public int getCoreLayout() {
        return coreLayout;
    }

    public void setCoreLayout(int coreLayout) {
        this.coreLayout = coreLayout;
    }

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public int getStereoDownmix() {
        return stereoDownmix;
    }

    public void setStereoDownmix(int stereoDownmix) {
        this.stereoDownmix = stereoDownmix;
    }

    public int getRepresentationType() {
        return representationType;
    }

    public void setRepresentationType(int representationType) {
        this.representationType = representationType;
    }

    public int getChannelLayout() {
        return channelLayout;
    }

    public void setChannelLayout(int channelLayout) {
        this.channelLayout = channelLayout;
    }

    public int getMultiAssetFlag() {
        return multiAssetFlag;
    }

    public void setMultiAssetFlag(int multiAssetFlag) {
        this.multiAssetFlag = multiAssetFlag;
    }

    public int getLBRDurationMod() {
        return LBRDurationMod;
    }

    public void setLBRDurationMod(int LBRDurationMod) {
        this.LBRDurationMod = LBRDurationMod;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public int getReservedBoxPresent() {
        return reservedBoxPresent;
    }

    public void setReservedBoxPresent(int reservedBoxPresent) {
        this.reservedBoxPresent = reservedBoxPresent;
    }

    @DoNotParseDetail
    public int[] getDashAudioChannelConfiguration() {
        final int channelLayout = this.getChannelLayout();
        int numChannels = 0;
        int dwChannelMask = 0;
        if ((channelLayout & 0x0001) == 0x0001) {
            //0001h Center in front of listener 1
            numChannels += 1;
            dwChannelMask |= 0x00000004; //SPEAKER_FRONT_CENTER
        }
        if ((channelLayout & 0x0002) == 0x0002) {
            //0002h Left/Right in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000001; //SPEAKER_FRONT_LEFT
            dwChannelMask |= 0x00000002; //SPEAKER_FRONT_RIGHT
        }
        if ((channelLayout & 0x0004) == 0x0004) {
            //0004h Left/Right surround on side in rear 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0008) == 0x0008) {
            //0008h Low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x0010) == 0x0010) {
            //0010h Center surround in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00000100; //SPEAKER_BACK_CENTER
        }
        if ((channelLayout & 0x0020) == 0x0020) {
            //0020h Left/Right height in front 2
            numChannels += 2;
            dwChannelMask |= 0x00001000; //SPEAKER_TOP_FRONT_LEFT
            dwChannelMask |= 0x00004000; //SPEAKER_TOP_FRONT_RIGHT
        }
        if ((channelLayout & 0x0040) == 0x0040) {
            //0040h Left/Right surround in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0080) == 0x0080) {
            //0080h Center Height in front 1
            numChannels += 1;
            dwChannelMask |= 0x00002000; //SPEAKER_TOP_FRONT_CENTER
        }
        if ((channelLayout & 0x0100) == 0x0100) {
            //0100h Over the listener’s head 1
            numChannels += 1;
            dwChannelMask |= 0x00000800; //SPEAKER_TOP_CENTER
        }
        if ((channelLayout & 0x0200) == 0x0200) {
            //0200h Between left/right and center in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000040; //SPEAKER_FRONT_LEFT_OF_CENTER
            dwChannelMask |= 0x00000080; //SPEAKER_FRONT_RIGHT_OF_CENTER
        }
        if ((channelLayout & 0x0400) == 0x0400) {
            //0400h Left/Right on side in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000200; //SPEAKER_SIDE_LEFT
            dwChannelMask |= 0x00000400; //SPEAKER_SIDE_RIGHT
        }
        if ((channelLayout & 0x0800) == 0x0800) {
            //0800h Left/Right surround on side 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x1000) == 0x1000) {
            //1000h Second low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x2000) == 0x2000) {
            //2000h Left/Right height on side 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x4000) == 0x4000) {
            //4000h Center height in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00010000; //SPEAKER_TOP_BACK_CENTER
        }
        if ((channelLayout & 0x8000) == 0x8000) {
            //8000h Left/Right height in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00008000; //SPEAKER_TOP_BACK_LEFT
            dwChannelMask |= 0x00020000; //SPEAKER_TOP_BACK_RIGHT
        }
        if ((channelLayout & 0x10000) == 0x10000) {
            //10000h Center below in front
            numChannels += 1;
        }
        if ((channelLayout & 0x20000) == 0x20000) {
            //20000h Left/Right below in front
            numChannels += 2;
        }
        return new int[]{numChannels, dwChannelMask};
    }


}
