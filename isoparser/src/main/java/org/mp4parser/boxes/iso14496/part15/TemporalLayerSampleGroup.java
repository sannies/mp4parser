package org.mp4parser.boxes.iso14496.part15;

import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;

/**
 * Created by sannies on 08.09.2014.
 */
public class TemporalLayerSampleGroup extends GroupEntry {
    public static final String TYPE = "tscl";

    int temporalLayerId;
    int tlprofile_space;
    boolean tltier_flag;
    int tlprofile_idc;
    long tlprofile_compatibility_flags;
    long tlconstraint_indicator_flags;
    int tllevel_idc;
    int tlMaxBitRate;
    int tlAvgBitRate;
    int tlConstantFrameRate;
    int tlAvgFrameRate;

    @Override
    public String getType() {
        return TYPE;
    }

    public int getTemporalLayerId() {
        return temporalLayerId;
    }

    public void setTemporalLayerId(int temporalLayerId) {
        this.temporalLayerId = temporalLayerId;
    }

    public int getTlprofile_space() {
        return tlprofile_space;
    }

    public void setTlprofile_space(int tlprofile_space) {
        this.tlprofile_space = tlprofile_space;
    }

    public boolean isTltier_flag() {
        return tltier_flag;
    }

    public void setTltier_flag(boolean tltier_flag) {
        this.tltier_flag = tltier_flag;
    }

    public int getTlprofile_idc() {
        return tlprofile_idc;
    }

    public void setTlprofile_idc(int tlprofile_idc) {
        this.tlprofile_idc = tlprofile_idc;
    }

    public long getTlprofile_compatibility_flags() {
        return tlprofile_compatibility_flags;
    }

    public void setTlprofile_compatibility_flags(long tlprofile_compatibility_flags) {
        this.tlprofile_compatibility_flags = tlprofile_compatibility_flags;
    }

    public long getTlconstraint_indicator_flags() {
        return tlconstraint_indicator_flags;
    }

    public void setTlconstraint_indicator_flags(long tlconstraint_indicator_flags) {
        this.tlconstraint_indicator_flags = tlconstraint_indicator_flags;
    }

    public int getTllevel_idc() {
        return tllevel_idc;
    }

    public void setTllevel_idc(int tllevel_idc) {
        this.tllevel_idc = tllevel_idc;
    }

    public int getTlMaxBitRate() {
        return tlMaxBitRate;
    }

    public void setTlMaxBitRate(int tlMaxBitRate) {
        this.tlMaxBitRate = tlMaxBitRate;
    }

    public int getTlAvgBitRate() {
        return tlAvgBitRate;
    }

    public void setTlAvgBitRate(int tlAvgBitRate) {
        this.tlAvgBitRate = tlAvgBitRate;
    }

    public int getTlConstantFrameRate() {
        return tlConstantFrameRate;
    }

    public void setTlConstantFrameRate(int tlConstantFrameRate) {
        this.tlConstantFrameRate = tlConstantFrameRate;
    }

    public int getTlAvgFrameRate() {
        return tlAvgFrameRate;
    }

    public void setTlAvgFrameRate(int tlAvgFrameRate) {
        this.tlAvgFrameRate = tlAvgFrameRate;
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        temporalLayerId = IsoTypeReader.readUInt8(byteBuffer);
        int a = IsoTypeReader.readUInt8(byteBuffer);
        tlprofile_space = (a & 0xC0) >> 6;
        tltier_flag = (a & 0x20) > 0;
        tlprofile_idc = (a & 0x1F);
        tlprofile_compatibility_flags = IsoTypeReader.readUInt32(byteBuffer);
        tlconstraint_indicator_flags = IsoTypeReader.readUInt48(byteBuffer);
        tllevel_idc = IsoTypeReader.readUInt8(byteBuffer);
        tlMaxBitRate = IsoTypeReader.readUInt16(byteBuffer);
        tlAvgBitRate = IsoTypeReader.readUInt16(byteBuffer);
        tlConstantFrameRate = IsoTypeReader.readUInt8(byteBuffer);
        tlAvgFrameRate = IsoTypeReader.readUInt16(byteBuffer);
    }

    @Override
    public ByteBuffer get() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        IsoTypeWriter.writeUInt8(byteBuffer, temporalLayerId);
        IsoTypeWriter.writeUInt8(byteBuffer, (tlprofile_space << 6) + (tltier_flag ? 0x20 : 0) + tlprofile_idc);

        IsoTypeWriter.writeUInt32(byteBuffer, tlprofile_compatibility_flags);
        IsoTypeWriter.writeUInt48(byteBuffer, tlconstraint_indicator_flags);
        IsoTypeWriter.writeUInt8(byteBuffer, tllevel_idc);
        IsoTypeWriter.writeUInt16(byteBuffer, tlMaxBitRate);
        IsoTypeWriter.writeUInt16(byteBuffer, tlAvgBitRate);
        IsoTypeWriter.writeUInt8(byteBuffer, tlConstantFrameRate);
        IsoTypeWriter.writeUInt16(byteBuffer, tlAvgFrameRate);
        return (ByteBuffer) byteBuffer.rewind();
    }

    @Override
    public int size() {
        return 20;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemporalLayerSampleGroup that = (TemporalLayerSampleGroup) o;

        if (temporalLayerId != that.temporalLayerId) return false;
        if (tlAvgBitRate != that.tlAvgBitRate) return false;
        if (tlAvgFrameRate != that.tlAvgFrameRate) return false;
        if (tlConstantFrameRate != that.tlConstantFrameRate) return false;
        if (tlMaxBitRate != that.tlMaxBitRate) return false;
        if (tlconstraint_indicator_flags != that.tlconstraint_indicator_flags) return false;
        if (tllevel_idc != that.tllevel_idc) return false;
        if (tlprofile_compatibility_flags != that.tlprofile_compatibility_flags) return false;
        if (tlprofile_idc != that.tlprofile_idc) return false;
        if (tlprofile_space != that.tlprofile_space) return false;
        if (tltier_flag != that.tltier_flag) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = temporalLayerId;
        result = 31 * result + tlprofile_space;
        result = 31 * result + (tltier_flag ? 1 : 0);
        result = 31 * result + tlprofile_idc;
        result = 31 * result + (int) (tlprofile_compatibility_flags ^ (tlprofile_compatibility_flags >>> 32));
        result = 31 * result + (int) (tlconstraint_indicator_flags ^ (tlconstraint_indicator_flags >>> 32));
        result = 31 * result + tllevel_idc;
        result = 31 * result + tlMaxBitRate;
        result = 31 * result + tlAvgBitRate;
        result = 31 * result + tlConstantFrameRate;
        result = 31 * result + tlAvgFrameRate;
        return result;
    }

    @Override
    public String toString() {
        return "TemporalLayerSampleGroup{" +
                "temporalLayerId=" + temporalLayerId +
                ", tlprofile_space=" + tlprofile_space +
                ", tltier_flag=" + tltier_flag +
                ", tlprofile_idc=" + tlprofile_idc +
                ", tlprofile_compatibility_flags=" + tlprofile_compatibility_flags +
                ", tlconstraint_indicator_flags=" + tlconstraint_indicator_flags +
                ", tllevel_idc=" + tllevel_idc +
                ", tlMaxBitRate=" + tlMaxBitRate +
                ", tlAvgBitRate=" + tlAvgBitRate +
                ", tlConstantFrameRate=" + tlConstantFrameRate +
                ", tlAvgFrameRate=" + tlAvgFrameRate +
                '}';
    }
}
