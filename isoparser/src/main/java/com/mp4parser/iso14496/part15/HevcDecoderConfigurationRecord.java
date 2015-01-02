package com.mp4parser.iso14496.part15;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class HevcDecoderConfigurationRecord {
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

    List<Array> arrays = new ArrayList<Array>();

    boolean frame_only_constraint_flag;
    boolean non_packed_constraint_flag;
    boolean interlaced_source_flag;
    boolean progressive_source_flag;


    public HevcDecoderConfigurationRecord() {
    }


    public void parse(ByteBuffer content) {
        configurationVersion = IsoTypeReader.readUInt8(content);

        /*
         * unsigned int(2) general_profile_space;
         * unsigned int(1) general_tier_flag;
         * unsigned int(5) general_profile_idc;
         */
        int a = IsoTypeReader.readUInt8(content);
        general_profile_space = (a & 0xC0) >> 6;
        general_tier_flag = (a & 0x20) > 0;
        general_profile_idc = (a & 0x1F);

        /* unsigned int(32) general_profile_compatibility_flags; */
        general_profile_compatibility_flags = IsoTypeReader.readUInt32(content);


         /* unsigned int(48) general_constraint_indicator_flags; */
        general_constraint_indicator_flags = IsoTypeReader.readUInt48(content);

        frame_only_constraint_flag = ((general_constraint_indicator_flags >> 44) & 0x08) > 0;
        non_packed_constraint_flag = ((general_constraint_indicator_flags >> 44) & 0x04) > 0;
        interlaced_source_flag = ((general_constraint_indicator_flags >> 44) & 0x02) > 0;
        progressive_source_flag = ((general_constraint_indicator_flags >> 44) & 0x01) > 0;

        general_constraint_indicator_flags &= 0x7fffffffffffL;

        /* unsigned int(8) general_level_idc; */
        general_level_idc = IsoTypeReader.readUInt8(content);

        /*
         * bit(4) reserved = ‘1111’b;
         * unsigned int(12) min_spatial_segmentation_idc;
         */
        a = IsoTypeReader.readUInt16(content);
        reserved1 = (a & 0xF000) >> 12;
        min_spatial_segmentation_idc = a & 0xFFF;

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
        lengthSizeMinusOne = a & 0x3;


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
        long _general_constraint_indicator_flags = general_constraint_indicator_flags;
        if (frame_only_constraint_flag) {
            _general_constraint_indicator_flags |= 1l << 47;
        }
        if (non_packed_constraint_flag) {
            _general_constraint_indicator_flags |= 1l << 46;
        }
        if ( interlaced_source_flag) {
            _general_constraint_indicator_flags |= 1l << 45;
        }
        if (progressive_source_flag) {
            _general_constraint_indicator_flags |= 1l << 44;
        }

        IsoTypeWriter.writeUInt48(byteBuffer, _general_constraint_indicator_flags);


        IsoTypeWriter.writeUInt8(byteBuffer, general_level_idc);

        IsoTypeWriter.writeUInt16(byteBuffer, (reserved1 << 12) + min_spatial_segmentation_idc);

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
        int size = 23;
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

        HevcDecoderConfigurationRecord that = (HevcDecoderConfigurationRecord) o;

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

    @Override
    public String toString() {
        return "HEVCDecoderConfigurationRecord{" +
                "configurationVersion=" + configurationVersion +
                ", general_profile_space=" + general_profile_space +
                ", general_tier_flag=" + general_tier_flag +
                ", general_profile_idc=" + general_profile_idc +
                ", general_profile_compatibility_flags=" + general_profile_compatibility_flags +
                ", general_constraint_indicator_flags=" + general_constraint_indicator_flags +
                ", general_level_idc=" + general_level_idc +
                (reserved1 != 0xf ? (", reserved1=" + reserved1) : "") +
                ", min_spatial_segmentation_idc=" + min_spatial_segmentation_idc +
                (reserved2 != 0x3F ? (", reserved2=" + reserved2) : "") +
                ", parallelismType=" + parallelismType +
                (reserved3 != 0x3F ? (", reserved3=" + reserved3) : "") +
                ", chromaFormat=" + chromaFormat +
                (reserved4 != 0x1F ? (", reserved4=" + reserved4) : "") +
                ", bitDepthLumaMinus8=" + bitDepthLumaMinus8 +
                (reserved5 != 0x1F ? (", reserved5=" + reserved5) : "") +
                ", bitDepthChromaMinus8=" + bitDepthChromaMinus8 +
                ", avgFrameRate=" + avgFrameRate +
                ", constantFrameRate=" + constantFrameRate +
                ", numTemporalLayers=" + numTemporalLayers +
                ", temporalIdNested=" + temporalIdNested +
                ", lengthSizeMinusOne=" + lengthSizeMinusOne +
                ", arrays=" + arrays +
                '}';
    }

    public int getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(int configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public int getGeneral_profile_space() {
        return general_profile_space;
    }

    public void setGeneral_profile_space(int general_profile_space) {
        this.general_profile_space = general_profile_space;
    }

    public boolean isGeneral_tier_flag() {
        return general_tier_flag;
    }

    public void setGeneral_tier_flag(boolean general_tier_flag) {
        this.general_tier_flag = general_tier_flag;
    }

    public int getGeneral_profile_idc() {
        return general_profile_idc;
    }

    public void setGeneral_profile_idc(int general_profile_idc) {
        this.general_profile_idc = general_profile_idc;
    }

    public long getGeneral_profile_compatibility_flags() {
        return general_profile_compatibility_flags;
    }

    public void setGeneral_profile_compatibility_flags(long general_profile_compatibility_flags) {
        this.general_profile_compatibility_flags = general_profile_compatibility_flags;
    }

    public long getGeneral_constraint_indicator_flags() {
        return general_constraint_indicator_flags;
    }

    public void setGeneral_constraint_indicator_flags(long general_constraint_indicator_flags) {
        this.general_constraint_indicator_flags = general_constraint_indicator_flags;
    }

    public int getGeneral_level_idc() {
        return general_level_idc;
    }

    public void setGeneral_level_idc(int general_level_idc) {
        this.general_level_idc = general_level_idc;
    }

    public int getMin_spatial_segmentation_idc() {
        return min_spatial_segmentation_idc;
    }

    public void setMin_spatial_segmentation_idc(int min_spatial_segmentation_idc) {
        this.min_spatial_segmentation_idc = min_spatial_segmentation_idc;
    }

    public int getParallelismType() {
        return parallelismType;
    }

    public void setParallelismType(int parallelismType) {
        this.parallelismType = parallelismType;
    }

    public int getChromaFormat() {
        return chromaFormat;
    }

    public void setChromaFormat(int chromaFormat) {
        this.chromaFormat = chromaFormat;
    }

    public int getBitDepthLumaMinus8() {
        return bitDepthLumaMinus8;
    }

    public void setBitDepthLumaMinus8(int bitDepthLumaMinus8) {
        this.bitDepthLumaMinus8 = bitDepthLumaMinus8;
    }

    public int getBitDepthChromaMinus8() {
        return bitDepthChromaMinus8;
    }

    public void setBitDepthChromaMinus8(int bitDepthChromaMinus8) {
        this.bitDepthChromaMinus8 = bitDepthChromaMinus8;
    }

    public int getAvgFrameRate() {
        return avgFrameRate;
    }

    public void setAvgFrameRate(int avgFrameRate) {
        this.avgFrameRate = avgFrameRate;
    }

    public int getNumTemporalLayers() {
        return numTemporalLayers;
    }

    public void setNumTemporalLayers(int numTemporalLayers) {
        this.numTemporalLayers = numTemporalLayers;
    }

    public int getLengthSizeMinusOne() {
        return lengthSizeMinusOne;
    }

    public void setLengthSizeMinusOne(int lengthSizeMinusOne) {
        this.lengthSizeMinusOne = lengthSizeMinusOne;
    }

    public boolean isTemporalIdNested() {
        return temporalIdNested;
    }

    public void setTemporalIdNested(boolean temporalIdNested) {
        this.temporalIdNested = temporalIdNested;
    }

    public int getConstantFrameRate() {
        return constantFrameRate;
    }

    public void setConstantFrameRate(int constantFrameRate) {
        this.constantFrameRate = constantFrameRate;
    }

    public List<Array> getArrays() {
        return arrays;
    }

    public void setArrays(List<Array> arrays) {
        this.arrays = arrays;
    }

    public boolean isFrame_only_constraint_flag() {
        return frame_only_constraint_flag;
    }

    public void setFrame_only_constraint_flag(boolean frame_only_constraint_flag) {
        this.frame_only_constraint_flag = frame_only_constraint_flag;
    }

    public boolean isNon_packed_constraint_flag() {
        return non_packed_constraint_flag;
    }

    public void setNon_packed_constraint_flag(boolean non_packed_constraint_flag) {
        this.non_packed_constraint_flag = non_packed_constraint_flag;
    }

    public boolean isInterlaced_source_flag() {
        return interlaced_source_flag;
    }

    public void setInterlaced_source_flag(boolean interlaced_source_flag) {
        this.interlaced_source_flag = interlaced_source_flag;
    }

    public boolean isProgressive_source_flag() {
        return progressive_source_flag;
    }

    public void setProgressive_source_flag(boolean progressive_source_flag) {
        this.progressive_source_flag = progressive_source_flag;
    }

    public static class Array {

        public boolean array_completeness;
        public boolean reserved;
        public int nal_unit_type;
        public List<byte[]> nalUnits;


        public Array() {

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Array array = (Array) o;

            if (array_completeness != array.array_completeness) return false;
            if (nal_unit_type != array.nal_unit_type) return false;
            if (reserved != array.reserved) return false;
            ListIterator<byte[]> e1 = nalUnits.listIterator();
            ListIterator<byte[]> e2 = (array.nalUnits).listIterator();
            while (e1.hasNext() && e2.hasNext()) {
                byte[] o1 = e1.next();
                byte[] o2 = e2.next();

                if (!(o1 == null ? o2 == null : Arrays.equals(o1, o2)))
                    return false;
            }
            return !(e1.hasNext() || e2.hasNext());
        }

        @Override
        public int hashCode() {
            int result = (array_completeness ? 1 : 0);
            result = 31 * result + (reserved ? 1 : 0);
            result = 31 * result + nal_unit_type;
            result = 31 * result + (nalUnits != null ? nalUnits.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Array{" +
                    "nal_unit_type=" + nal_unit_type +
                    ", reserved=" + reserved +
                    ", array_completeness=" + array_completeness +
                    ", num_nals=" + nalUnits.size() +
                    '}';
        }
    }
}
