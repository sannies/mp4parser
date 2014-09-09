package com.mp4parser.iso14496.part15;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * unsigned int(8) numOfArrays;
 * <p/>
 * for (j=0; j < numOfArrays; j++) {
 * <p/>
 * bit(1) array_completeness;
 * unsigned int(1) reserved = 0;
 * unsigned int(6) NAL_unit_type;
 * <p/>
 * unsigned int(16) numNalus;
 * for (i=0; i< numNalus; i++) {
 * unsigned int(16) nalUnitLength;
 * bit(8*nalUnitLength) nalUnit;
 * }
 * }
 */
public class HEVCDecoderConfigurationRecord {
    int configurationVersion;

    int general_profile_space;
    boolean general_tier_flag;
    int general_profile_idc;

    long general_profile_compatibility_flags;
    long general_constraint_indicator_flags;

    int general_level_idc;

    int reserved1 = 0xF;
    int min_spatial_segmentation_idc;

    int reserved2 = 0x3F;
    int parallelismType;

    int reserved3 = 0x3F;
    int chromaFormat;

    int reserved4 = 0x1F;
    int bitDepthLumaMinus8;

    int reserved5 = 0x1F;
    int bitDepthChromaMinus8;

    int avgFrameRate;

    int constantFrameRate;
    int numTemporalLayers;
    boolean temporalIdNested;
    int lengthSizeMinusOne;

    List<Array> arrays;


    public HEVCDecoderConfigurationRecord() {
    }


    public void parse(ByteBuffer content) {
        configurationVersion = IsoTypeReader.readUInt8(content);

        int a = IsoTypeReader.readUInt8(content);
        general_profile_space = (a & 0xC0) >> 6;
        general_tier_flag = (a & 0x20) > 0;
        general_profile_idc = (a & 0x1F);

        general_profile_compatibility_flags = IsoTypeReader.readUInt32(content);
        general_constraint_indicator_flags = IsoTypeReader.readUInt48(content);

        general_level_idc = IsoTypeReader.readUInt8(content);

        a = IsoTypeReader.readUInt8(content);
        reserved1 = (a & 0xF0) >> 4;
        min_spatial_segmentation_idc = a & 0xF;

        a = IsoTypeReader.readUInt8(content);
        reserved2 = (a & 0xFC) >> 2;
        parallelismType = a & 0x3;

        a = IsoTypeReader.readUInt8(content);
        reserved3 = (a & 0xFC) >> 2;
        chromaFormat = a & 0x3;

        a = IsoTypeReader.readUInt8(content);
        reserved4 = (a & 0xF8) >> 3;
        bitDepthLumaMinus8 = a & 0x7;

        a = IsoTypeReader.readUInt8(content);
        reserved5 = (a & 0xF8) >> 3;
        bitDepthChromaMinus8 = a & 0x7;

        avgFrameRate = IsoTypeReader.readUInt16(content);

        a = IsoTypeReader.readUInt8(content);
        constantFrameRate = (a & 0xC0) >> 6;
        numTemporalLayers = (a & 0x38) >> 3;
        temporalIdNested = (a & 0x4) > 0;
        lengthSizeMinusOne = a & 0x2;


        int numOfArrays = IsoTypeReader.readUInt8(content);
        this.arrays = new ArrayList<Array>();
        for (int i = 0; i < numOfArrays; i++) {
            Array array = new Array();

            a = IsoTypeReader.readUInt8(content);
            array.array_completeness = (a & 0x80) > 0;
            array.reserved = (a & 0x40) > 0;
            array.nal_unit_type = a & 0x3F;

            int numNalus = IsoTypeReader.readUInt16(content);
            array.nalUnits = new ArrayList<byte[]>();
            for (int j = 0; j < numNalus; j++) {
                int nalUnitLength = IsoTypeReader.readUInt16(content);
                byte[] nal = new byte[nalUnitLength];
                content.get(nal);
                array.nalUnits.add(nal);
            }
            arrays.add(array);
        }
    }

    public void write(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt8(byteBuffer, configurationVersion);


        IsoTypeWriter.writeUInt8(byteBuffer, (general_profile_space << 6) + (general_tier_flag ? 0x20 : 0) + general_profile_idc);

        IsoTypeWriter.writeUInt32(byteBuffer, general_profile_compatibility_flags);
        IsoTypeWriter.writeUInt48(byteBuffer, general_constraint_indicator_flags);


        IsoTypeWriter.writeUInt8(byteBuffer, general_level_idc);

        IsoTypeWriter.writeUInt8(byteBuffer, (reserved1 << 4) + min_spatial_segmentation_idc);

        IsoTypeWriter.writeUInt8(byteBuffer, (reserved2 << 2) + parallelismType);

        IsoTypeWriter.writeUInt8(byteBuffer, (reserved3 << 2) + chromaFormat);

        IsoTypeWriter.writeUInt8(byteBuffer, (reserved4 << 3) + bitDepthLumaMinus8);

        IsoTypeWriter.writeUInt8(byteBuffer, (reserved5 << 3) + bitDepthChromaMinus8);

        IsoTypeWriter.writeUInt16(byteBuffer, avgFrameRate);

        IsoTypeWriter.writeUInt8(byteBuffer, (constantFrameRate << 6) + (numTemporalLayers << 3) + (temporalIdNested ? 0x4 : 0) + lengthSizeMinusOne);

        IsoTypeWriter.writeUInt8(byteBuffer, arrays.size());

        for (Array array : arrays) {
            IsoTypeWriter.writeUInt8(byteBuffer, (array.array_completeness ? 0x80 : 0) + (array.reserved ? 0x40 : 0) + array.nal_unit_type);

            IsoTypeWriter.writeUInt16(byteBuffer, array.nalUnits.size());
            for (byte[] nalUnit : array.nalUnits) {
                IsoTypeWriter.writeUInt16(byteBuffer, nalUnit.length);
                byteBuffer.put(nalUnit);
            }
        }
    }

    public int getSize() {
        int size = 22;
        for (Array array : arrays) {
            size += 3;
            for (byte[] nalUnit : array.nalUnits) {
                size += 2;
                size += nalUnit.length;
            }
        }
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HEVCDecoderConfigurationRecord that = (HEVCDecoderConfigurationRecord) o;

        if (avgFrameRate != that.avgFrameRate) return false;
        if (bitDepthChromaMinus8 != that.bitDepthChromaMinus8) return false;
        if (bitDepthLumaMinus8 != that.bitDepthLumaMinus8) return false;
        if (chromaFormat != that.chromaFormat) return false;
        if (configurationVersion != that.configurationVersion) return false;
        if (constantFrameRate != that.constantFrameRate) return false;
        if (general_constraint_indicator_flags != that.general_constraint_indicator_flags) return false;
        if (general_level_idc != that.general_level_idc) return false;
        if (general_profile_compatibility_flags != that.general_profile_compatibility_flags) return false;
        if (general_profile_idc != that.general_profile_idc) return false;
        if (general_profile_space != that.general_profile_space) return false;
        if (general_tier_flag != that.general_tier_flag) return false;
        if (lengthSizeMinusOne != that.lengthSizeMinusOne) return false;
        if (min_spatial_segmentation_idc != that.min_spatial_segmentation_idc) return false;
        if (numTemporalLayers != that.numTemporalLayers) return false;
        if (parallelismType != that.parallelismType) return false;
        if (reserved1 != that.reserved1) return false;
        if (reserved2 != that.reserved2) return false;
        if (reserved3 != that.reserved3) return false;
        if (reserved4 != that.reserved4) return false;
        if (reserved5 != that.reserved5) return false;
        if (temporalIdNested != that.temporalIdNested) return false;
        if (arrays != null ? !arrays.equals(that.arrays) : that.arrays != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = configurationVersion;
        result = 31 * result + general_profile_space;
        result = 31 * result + (general_tier_flag ? 1 : 0);
        result = 31 * result + general_profile_idc;
        result = 31 * result + (int) (general_profile_compatibility_flags ^ (general_profile_compatibility_flags >>> 32));
        result = 31 * result + (int) (general_constraint_indicator_flags ^ (general_constraint_indicator_flags >>> 32));
        result = 31 * result + general_level_idc;
        result = 31 * result + reserved1;
        result = 31 * result + min_spatial_segmentation_idc;
        result = 31 * result + reserved2;
        result = 31 * result + parallelismType;
        result = 31 * result + reserved3;
        result = 31 * result + chromaFormat;
        result = 31 * result + reserved4;
        result = 31 * result + bitDepthLumaMinus8;
        result = 31 * result + reserved5;
        result = 31 * result + bitDepthChromaMinus8;
        result = 31 * result + avgFrameRate;
        result = 31 * result + constantFrameRate;
        result = 31 * result + numTemporalLayers;
        result = 31 * result + (temporalIdNested ? 1 : 0);
        result = 31 * result + lengthSizeMinusOne;
        result = 31 * result + (arrays != null ? arrays.hashCode() : 0);
        return result;
    }

    class Array {

        boolean array_completeness;
        boolean reserved;
        int nal_unit_type;
        List<byte[]> nalUnits;


        public Array() {

        }
    }
}
