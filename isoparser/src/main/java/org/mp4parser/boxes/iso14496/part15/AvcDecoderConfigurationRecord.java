package org.mp4parser.boxes.iso14496.part15;

import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitWriterBuffer;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AvcDecoderConfigurationRecord {
    public int configurationVersion;
    public int avcProfileIndication;
    public int profileCompatibility;
    public int avcLevelIndication;
    public int lengthSizeMinusOne;
    public List<ByteBuffer> sequenceParameterSets = new ArrayList<ByteBuffer>();
    public List<ByteBuffer> pictureParameterSets = new ArrayList<ByteBuffer>();

    public boolean hasExts = true;
    public int chromaFormat = 1;
    public int bitDepthLumaMinus8 = 0;
    public int bitDepthChromaMinus8 = 0;
    public List<ByteBuffer> sequenceParameterSetExts = new ArrayList<ByteBuffer>();

    /**
     * Just for non-spec-conform encoders
     */
    public int lengthSizeMinusOnePaddingBits = 63;
    public int numberOfSequenceParameterSetsPaddingBits = 7;
    public int chromaFormatPaddingBits = 31;
    public int bitDepthLumaMinus8PaddingBits = 31;
    public int bitDepthChromaMinus8PaddingBits = 31;

    public AvcDecoderConfigurationRecord() {
    }

    public AvcDecoderConfigurationRecord(ByteBuffer content) {
        configurationVersion = IsoTypeReader.readUInt8(content);
        avcProfileIndication = IsoTypeReader.readUInt8(content);
        profileCompatibility = IsoTypeReader.readUInt8(content);
        avcLevelIndication = IsoTypeReader.readUInt8(content);
        BitReaderBuffer brb = new BitReaderBuffer(content);
        lengthSizeMinusOnePaddingBits = brb.readBits(6);
        lengthSizeMinusOne = brb.readBits(2);
        numberOfSequenceParameterSetsPaddingBits = brb.readBits(3);
        int numberOfSeuqenceParameterSets = brb.readBits(5);
        for (int i = 0; i < numberOfSeuqenceParameterSets; i++) {
            int sequenceParameterSetLength = IsoTypeReader.readUInt16(content);

            byte[] sequenceParameterSetNALUnit = new byte[sequenceParameterSetLength];
            content.get(sequenceParameterSetNALUnit);
            sequenceParameterSets.add(ByteBuffer.wrap(sequenceParameterSetNALUnit));
        }
        long numberOfPictureParameterSets = IsoTypeReader.readUInt8(content);
        for (int i = 0; i < numberOfPictureParameterSets; i++) {
            int pictureParameterSetLength = IsoTypeReader.readUInt16(content);
            byte[] pictureParameterSetNALUnit = new byte[pictureParameterSetLength];
            content.get(pictureParameterSetNALUnit);
            pictureParameterSets.add(ByteBuffer.wrap(pictureParameterSetNALUnit));
        }
        if (content.remaining() < 4) {
            hasExts = false;
        }
        if (hasExts && (avcProfileIndication == 100 || avcProfileIndication == 110 || avcProfileIndication == 122 || avcProfileIndication == 144)) {
            // actually only some bits are interesting so masking with & x would be good but not all Mp4 creating tools set the reserved bits to 1.
            // So we need to store all bits
            brb = new BitReaderBuffer(content);
            chromaFormatPaddingBits = brb.readBits(6);
            chromaFormat = brb.readBits(2);
            bitDepthLumaMinus8PaddingBits = brb.readBits(5);
            bitDepthLumaMinus8 = brb.readBits(3);
            bitDepthChromaMinus8PaddingBits = brb.readBits(5);
            bitDepthChromaMinus8 = brb.readBits(3);
            long numOfSequenceParameterSetExt = IsoTypeReader.readUInt8(content);
            for (int i = 0; i < numOfSequenceParameterSetExt; i++) {
                int sequenceParameterSetExtLength = IsoTypeReader.readUInt16(content);
                byte[] sequenceParameterSetExtNALUnit = new byte[sequenceParameterSetExtLength];
                content.get(sequenceParameterSetExtNALUnit);
                sequenceParameterSetExts.add(ByteBuffer.wrap(sequenceParameterSetExtNALUnit));
            }
        } else {
            chromaFormat = -1;
            bitDepthLumaMinus8 = -1;
            bitDepthChromaMinus8 = -1;
        }
    }

    public void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt8(byteBuffer, configurationVersion);
        IsoTypeWriter.writeUInt8(byteBuffer, avcProfileIndication);
        IsoTypeWriter.writeUInt8(byteBuffer, profileCompatibility);
        IsoTypeWriter.writeUInt8(byteBuffer, avcLevelIndication);
        BitWriterBuffer bwb = new BitWriterBuffer(byteBuffer);
        bwb.writeBits(lengthSizeMinusOnePaddingBits, 6);
        bwb.writeBits(lengthSizeMinusOne, 2);
        bwb.writeBits(numberOfSequenceParameterSetsPaddingBits, 3);
        bwb.writeBits(pictureParameterSets.size(), 5);
        for (ByteBuffer sequenceParameterSetNALUnit : sequenceParameterSets) {
            IsoTypeWriter.writeUInt16(byteBuffer, sequenceParameterSetNALUnit.limit());
            byteBuffer.put((ByteBuffer) ((Buffer)sequenceParameterSetNALUnit).rewind());
        }
        IsoTypeWriter.writeUInt8(byteBuffer, pictureParameterSets.size());
        for (ByteBuffer pictureParameterSetNALUnit : pictureParameterSets) {
            IsoTypeWriter.writeUInt16(byteBuffer, pictureParameterSetNALUnit.limit());
            byteBuffer.put((ByteBuffer) ((Buffer)pictureParameterSetNALUnit).rewind());
        }
        if (hasExts && (avcProfileIndication == 100 || avcProfileIndication == 110 || avcProfileIndication == 122 || avcProfileIndication == 144)) {

            bwb = new BitWriterBuffer(byteBuffer);
            bwb.writeBits(chromaFormatPaddingBits, 6);
            bwb.writeBits(chromaFormat, 2);
            bwb.writeBits(bitDepthLumaMinus8PaddingBits, 5);
            bwb.writeBits(bitDepthLumaMinus8, 3);
            bwb.writeBits(bitDepthChromaMinus8PaddingBits, 5);
            bwb.writeBits(bitDepthChromaMinus8, 3);
            for (ByteBuffer sequenceParameterSetExtNALUnit : sequenceParameterSetExts) {
                IsoTypeWriter.writeUInt16(byteBuffer, sequenceParameterSetExtNALUnit.limit());
                byteBuffer.put((ByteBuffer) sequenceParameterSetExtNALUnit.reset());
            }
        }
    }

    public long getContentSize() {
        long size = 5;
        size += 1; // sequenceParamsetLength
        for (ByteBuffer sequenceParameterSetNALUnit : sequenceParameterSets) {
            size += 2; //lengthSizeMinusOne field
            size += sequenceParameterSetNALUnit.limit();
        }
        size += 1; // pictureParamsetLength
        for (ByteBuffer pictureParameterSetNALUnit : pictureParameterSets) {
            size += 2; //lengthSizeMinusOne field
            size += pictureParameterSetNALUnit.limit();
        }
        if (hasExts && (avcProfileIndication == 100 || avcProfileIndication == 110 || avcProfileIndication == 122 || avcProfileIndication == 144)) {
            size += 4;
            for (ByteBuffer sequenceParameterSetExtNALUnit : sequenceParameterSetExts) {
                size += 2;
                size += sequenceParameterSetExtNALUnit.limit();
            }
        }

        return size;
    }


    public List<String> getSequenceParameterSetsAsStrings() {
        List<String> result = new ArrayList<String>(sequenceParameterSets.size());
        for (ByteBuffer parameterSet : sequenceParameterSets) {
            result.add(Hex.encodeHex(parameterSet));
        }
        return result;
    }

    public List<String> getSequenceParameterSetExtsAsStrings() {
        List<String> result = new ArrayList<String>(sequenceParameterSetExts.size());
        for (ByteBuffer parameterSet : sequenceParameterSetExts) {
            result.add(Hex.encodeHex(parameterSet));
        }
        return result;
    }

    public List<String> getPictureParameterSetsAsStrings() {
        List<String> result = new ArrayList<String>(pictureParameterSets.size());
        for (ByteBuffer parameterSet : pictureParameterSets) {
            result.add(Hex.encodeHex(parameterSet));
        }
        return result;
    }

    @Override
    public String toString() {
        return "AvcDecoderConfigurationRecord{" +
                "configurationVersion=" + configurationVersion +
                ", avcProfileIndication=" + avcProfileIndication +
                ", profileCompatibility=" + profileCompatibility +
                ", avcLevelIndication=" + avcLevelIndication +
                ", lengthSizeMinusOne=" + lengthSizeMinusOne +
                ", hasExts=" + hasExts +
                ", chromaFormat=" + chromaFormat +
                ", bitDepthLumaMinus8=" + bitDepthLumaMinus8 +
                ", bitDepthChromaMinus8=" + bitDepthChromaMinus8 +
                ", lengthSizeMinusOnePaddingBits=" + lengthSizeMinusOnePaddingBits +
                ", numberOfSequenceParameterSetsPaddingBits=" + numberOfSequenceParameterSetsPaddingBits +
                ", chromaFormatPaddingBits=" + chromaFormatPaddingBits +
                ", bitDepthLumaMinus8PaddingBits=" + bitDepthLumaMinus8PaddingBits +
                ", bitDepthChromaMinus8PaddingBits=" + bitDepthChromaMinus8PaddingBits +
                '}';
    }
}
