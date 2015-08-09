package com.mp4parser.muxer.tracks.h265;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.muxer.DataSource;
import com.mp4parser.muxer.FileDataSourceImpl;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.SampleImpl;
import com.mp4parser.muxer.tracks.h264.parsing.read.CAVLCReader;
import com.mp4parser.tools.ByteBufferByteChannel;
import com.mp4parser.boxes.iso14496.part15.HevcDecoderConfigurationRecord;

import java.io.EOFException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sannies on 08.09.2014.
 */
public class H265TrackImplOld {
    public static final int VPS_NUT = 32;
    public static final int SPS_NUT = 33;
    public static final int PPS_NUT = 34;
    public static final int AUD_NUT = 35;
    public static final int PREFIX_SEI_NUT = 39;
    public static final int RSV_NVCL41 = 41;
    public static final int RSV_NVCL42 = 42;
    public static final int RSV_NVCL43 = 43;
    public static final int RSV_NVCL44 = 44;
    public static final int UNSPEC48 = 48;
    public static final int UNSPEC49 = 49;
    public static final int UNSPEC50 = 50;
    public static final int UNSPEC51 = 51;
    public static final int UNSPEC52 = 52;
    public static final int UNSPEC53 = 53;
    public static final int UNSPEC54 = 54;
    public static final int UNSPEC55 = 55;
    private static final int TRAIL_N = 0;
    private static final int TRAIL_R = 1;
    private static final int TSA_N = 2;
    private static final int TSA_R = 3;
    private static final int STSA_N = 4;
    private static final int STSA_R = 5;
    private static final int RADL_N = 6;
    private static final int RADL_R = 7;
    private static final int RASL_N = 8;
    private static final int RASL_R = 9;
    private static final int BLA_W_LP = 16;
    private static final int BLA_W_RADL = 17;
    private static final int BLA_N_LP = 18;
    private static final int IDR_W_RADL = 19;
    private static final int IDR_N_LP = 20;
    private static final int CRA_NUT = 21;

    private static final long BUFFER = 1024 * 1024;
    LinkedHashMap<Long, ByteBuffer> videoParamterSets = new LinkedHashMap<Long, ByteBuffer>();
    LinkedHashMap<Long, ByteBuffer> sequenceParamterSets = new LinkedHashMap<Long, ByteBuffer>();
    LinkedHashMap<Long, ByteBuffer> pictureParamterSets = new LinkedHashMap<Long, ByteBuffer>();
    List<Long> syncSamples = new ArrayList<Long>();
    List<Sample> samples = new ArrayList<Sample>();

    public H265TrackImplOld(DataSource ds) throws IOException {
        LookAhead la = new LookAhead(ds);

        long sampleNo = 1;
        List<ByteBuffer> accessUnit = new ArrayList<ByteBuffer>();
        int accessUnitNalType = 0;

        ByteBuffer nal;
        while ((nal = findNextNal(la)) != null) {

            NalUnitHeader nalUnitHeader = getNalUnitHeader(nal);

            //System.err.println(String.format("type: %3d - layer: %3d - tempId: %3d",
            //        nalUnitHeader.nalUnitType, nalUnitHeader.nuhLayerId, nalUnitHeader.nuhTemporalIdPlusOne));
            switch (nalUnitHeader.nalUnitType) {
                case VPS_NUT:
                    videoParamterSets.put(sampleNo, nal);
                    break;
                case SPS_NUT:
                    sequenceParamterSets.put(sampleNo, nal);
                    break;
                case PPS_NUT:
                    pictureParamterSets.put(sampleNo, nal);
                    break;
            }
            if (nalUnitHeader.nalUnitType < 32) {
                accessUnitNalType = nalUnitHeader.nalUnitType;
                // All NAL in one Access Unit Sample have same nal unit type
            }

            if (isFirstOfAU(nalUnitHeader.nalUnitType, nal, accessUnit) && !accessUnit.isEmpty()) {

                System.err.println("##########################");
                for (ByteBuffer byteBuffer : accessUnit) {
                    NalUnitHeader _nalUnitHeader = getNalUnitHeader(byteBuffer);
                    System.err.println(String.format("type: %3d - layer: %3d - tempId: %3d - size: %3d",
                            _nalUnitHeader.nalUnitType, _nalUnitHeader.nuhLayerId, _nalUnitHeader.nuhTemporalIdPlusOne, byteBuffer.limit()));
                }

                System.err.println("                          ##########################");
                samples.add(createSample(accessUnit));
                accessUnit.clear();
                sampleNo++;
            }
            accessUnit.add(nal);
            if (accessUnitNalType >= 16 && accessUnitNalType <= 21) {
                syncSamples.add(sampleNo);
            }


        }

        System.err.println("");
        HevcDecoderConfigurationRecord hvcC = new HevcDecoderConfigurationRecord();

        hvcC.setArrays(getArrays());
        hvcC.setAvgFrameRate(0);
    }

    public static void main(String[] args) throws IOException {
        new H265TrackImplOld(new FileDataSourceImpl("c:\\content\\test-UHD-HEVC_01_FMV_Med_track1.hvc"));
    }

    private ByteBuffer findNextNal(LookAhead la) throws IOException {
        try {
            while (!la.nextThreeEquals001()) {
                la.discardByte();
            }
            la.discardNext3AndMarkStart();

            while (!la.nextThreeEquals000or001orEof()) {
                la.discardByte();
            }
            return la.getNal();
        } catch (EOFException e) {
            return null;
        }

    }

    public void profile_tier_level(int maxNumSubLayersMinus1, CAVLCReader r) throws IOException {
        int general_profile_space = r.readU(2, "general_profile_space ");
        boolean general_tier_flag = r.readBool("general_tier_flag");
        int general_profile_idc = r.readU(5, "general_profile_idc");
        boolean[] general_profile_compatibility_flag = new boolean[32];
        for (int j = 0; j < 32; j++) {
            general_profile_compatibility_flag[j] = r.readBool("general_profile_compatibility_flag[" + j + "]");
        }
        boolean general_progressive_source_flag = r.readBool("general_progressive_source_flag");
        boolean general_interlaced_source_flag = r.readBool("general_interlaced_source_flag");
        boolean general_non_packed_constraint_flag = r.readBool("general_non_packed_constraint_flag");
        boolean general_frame_only_constraint_flag = r.readBool("general_frame_only_constraint_flag");

        long general_reserved_zero_44bits = r.readU(44, "general_reserved_zero_44bits");
        int general_level_idc = r.readU(8, "general_level_idc");

        boolean[] sub_layer_profile_present_flag = new boolean[maxNumSubLayersMinus1];
        boolean[] sub_layer_level_present_flag = new boolean[maxNumSubLayersMinus1];
        for (int i = 0; i < maxNumSubLayersMinus1; i++) {
            sub_layer_profile_present_flag[i] = r.readBool("sub_layer_profile_present_flag[" + i + "]");
            sub_layer_level_present_flag[i] = r.readBool("sub_layer_level_present_flag[" + i + "]");
        }
        if (maxNumSubLayersMinus1 > 0) {
            for (int i = maxNumSubLayersMinus1; i < 8; i++) {
                r.readU(2, "reserved_zero_2bits");
            }
        }


        int[] sub_layer_profile_space = new int[maxNumSubLayersMinus1];
        boolean[] sub_layer_tier_flag = new boolean[maxNumSubLayersMinus1];
        int[] sub_layer_profile_idc = new int[maxNumSubLayersMinus1];
        boolean[][] sub_layer_profile_compatibility_flag = new boolean[maxNumSubLayersMinus1][32];
        boolean[] sub_layer_progressive_source_flag = new boolean[maxNumSubLayersMinus1];

        boolean[] sub_layer_interlaced_source_flag = new boolean[maxNumSubLayersMinus1];
        boolean[] sub_layer_non_packed_constraint_flag = new boolean[maxNumSubLayersMinus1];
        boolean[] sub_layer_frame_only_constraint_flag = new boolean[maxNumSubLayersMinus1];
        int[] sub_layer_level_idc = new int[maxNumSubLayersMinus1];

        for (int i = 0; i < maxNumSubLayersMinus1; i++) {
            if (sub_layer_profile_present_flag[i]) {
                sub_layer_profile_space[i] = r.readU(2, "sub_layer_profile_space[" + i + "]");
                sub_layer_tier_flag[i] = r.readBool("sub_layer_tier_flag[" + i + "]");
                sub_layer_profile_idc[i] = r.readU(5, "sub_layer_profile_idc[" + i + "]");
                for (int j = 0; j < 32; j++) {
                    sub_layer_profile_compatibility_flag[i][j] = r.readBool("sub_layer_profile_compatibility_flag[" + i + "][" + j + "]");
                }
                sub_layer_progressive_source_flag[i] = r.readBool("sub_layer_progressive_source_flag[" + i + "]");
                sub_layer_interlaced_source_flag[i] = r.readBool("sub_layer_interlaced_source_flag[" + i + "]");
                sub_layer_non_packed_constraint_flag[i] = r.readBool("sub_layer_non_packed_constraint_flag[" + i + "]");
                sub_layer_frame_only_constraint_flag[i] = r.readBool("sub_layer_frame_only_constraint_flag[" + i + "]");
                r.readNBit(44, "reserved");
            }
            if (sub_layer_level_present_flag[i])
                sub_layer_level_idc[i] = r.readU(8, "sub_layer_level_idc");
        }
    }


    public int getFrameRate(ByteBuffer vps) throws IOException {
        CAVLCReader r = new CAVLCReader(Channels.newInputStream(new ByteBufferByteChannel((ByteBuffer) vps.position(0))));
        int vps_parameter_set_id = r.readU(4, "vps_parameter_set_id");
        int vps_reserved_three_2bits = r.readU(2, "vps_reserved_three_2bits");
        int vps_max_layers_minus1 = r.readU(6, "vps_max_layers_minus1");
        int vps_max_sub_layers_minus1 = r.readU(3, "vps_max_sub_layers_minus1");
        boolean vps_temporal_id_nesting_flag = r.readBool("vps_temporal_id_nesting_flag");
        int vps_reserved_0xffff_16bits = r.readU(16, "vps_reserved_0xffff_16bits");
        profile_tier_level(vps_max_sub_layers_minus1, r);


        boolean vps_sub_layer_ordering_info_present_flag = r.readBool("vps_sub_layer_ordering_info_present_flag");
        int[] vps_max_dec_pic_buffering_minus1 = new int[vps_sub_layer_ordering_info_present_flag ? 0 : vps_max_sub_layers_minus1];
        int[] vps_max_num_reorder_pics = new int[vps_sub_layer_ordering_info_present_flag ? 0 : vps_max_sub_layers_minus1];
        int[] vps_max_latency_increase_plus1 = new int[vps_sub_layer_ordering_info_present_flag ? 0 : vps_max_sub_layers_minus1];
        for (int i = (vps_sub_layer_ordering_info_present_flag ? 0 : vps_max_sub_layers_minus1); i <= vps_max_sub_layers_minus1; i++) {
            vps_max_dec_pic_buffering_minus1[i] = r.readUE("vps_max_dec_pic_buffering_minus1[" + i + "]");
            vps_max_num_reorder_pics[i] = r.readUE("vps_max_dec_pic_buffering_minus1[" + i + "]");
            vps_max_latency_increase_plus1[i] = r.readUE("vps_max_dec_pic_buffering_minus1[" + i + "]");
        }
        int vps_max_layer_id = r.readU(6, "vps_max_layer_id");
        int vps_num_layer_sets_minus1 = r.readUE("vps_num_layer_sets_minus1");
        boolean[][] layer_id_included_flag = new boolean[vps_num_layer_sets_minus1][vps_max_layer_id];
        for (int i = 1; i <= vps_num_layer_sets_minus1; i++) {
            for (int j = 0; j <= vps_max_layer_id; j++) {
                layer_id_included_flag[i][j] = r.readBool("layer_id_included_flag[" + i + "][" + j + "]");
            }
        }
        boolean vps_timing_info_present_flag = r.readBool("vps_timing_info_present_flag");
        if (vps_timing_info_present_flag) {
            long vps_num_units_in_tick = r.readU(32, "vps_num_units_in_tick");
            long vps_time_scale = r.readU(32, "vps_time_scale");
            boolean vps_poc_proportional_to_timing_flag = r.readBool("vps_poc_proportional_to_timing_flag");
            if (vps_poc_proportional_to_timing_flag) {
                int vps_num_ticks_poc_diff_one_minus1 = r.readUE("vps_num_ticks_poc_diff_one_minus1");
            }
            int vps_num_hrd_parameters = r.readUE("vps_num_hrd_parameters");
            int hrd_layer_set_idx[] = new int[vps_num_hrd_parameters];
            boolean cprms_present_flag[] = new boolean[vps_num_hrd_parameters];
            for (int i = 0; i < vps_num_hrd_parameters; i++) {
                hrd_layer_set_idx[i] = r.readUE("hrd_layer_set_idx[" + i + "]");
                if (i > 0) {
                    cprms_present_flag[i] = r.readBool("cprms_present_flag[" + i + "]");
                } else {
                    cprms_present_flag[0] = true;
                }

                hrd_parameters(cprms_present_flag[i], vps_max_sub_layers_minus1, r);
            }
        }

        boolean vps_extension_flag = r.readBool("vps_extension_flag");
        if (vps_extension_flag) {
            while (r.moreRBSPData()) {
                boolean vps_extension_data_flag = r.readBool("vps_extension_data_flag");
            }
        }
        r.readTrailingBits();
        return 0;
    }

    private void hrd_parameters(boolean commonInfPresentFlag, int maxNumSubLayersMinus1, CAVLCReader r) throws IOException {
        boolean nal_hrd_parameters_present_flag = false;
        boolean vcl_hrd_parameters_present_flag = false;
        boolean sub_pic_hrd_params_present_flag = false;
        if (commonInfPresentFlag) {
            nal_hrd_parameters_present_flag = r.readBool("nal_hrd_parameters_present_flag");
            vcl_hrd_parameters_present_flag = r.readBool("vcl_hrd_parameters_present_flag");
            if (nal_hrd_parameters_present_flag || vcl_hrd_parameters_present_flag) {
                sub_pic_hrd_params_present_flag = r.readBool("sub_pic_hrd_params_present_flag");
                if (sub_pic_hrd_params_present_flag) {
                    int tick_divisor_minus2 = r.readU(8, "tick_divisor_minus2");
                    int du_cpb_removal_delay_increment_length_minus1 = r.readU(5, "du_cpb_removal_delay_increment_length_minus1");
                    boolean sub_pic_cpb_params_in_pic_timing_sei_flag = r.readBool("sub_pic_cpb_params_in_pic_timing_sei_flag");
                    int dpb_output_delay_du_length_minus1 = r.readU(5, "dpb_output_delay_du_length_minus1");

                }
                int bit_rate_scale = r.readU(4, "bit_rate_scale");
                int cpb_size_scale = r.readU(4, "cpb_size_scale");
                if (sub_pic_hrd_params_present_flag) {
                    int cpb_size_du_scale = r.readU(4, "cpb_size_du_scale");
                }
                int initial_cpb_removal_delay_length_minus1 = r.readU(5, "initial_cpb_removal_delay_length_minus1");
                int au_cpb_removal_delay_length_minus1 = r.readU(5, "au_cpb_removal_delay_length_minus1");
                int dpb_output_delay_length_minus1 = r.readU(5, "dpb_output_delay_length_minus1");
            }
        }
        boolean fixed_pic_rate_general_flag[] = new boolean[maxNumSubLayersMinus1];
        boolean fixed_pic_rate_within_cvs_flag[] = new boolean[maxNumSubLayersMinus1];
        boolean low_delay_hrd_flag[] = new boolean[maxNumSubLayersMinus1];
        int cpb_cnt_minus1[] = new int[maxNumSubLayersMinus1];
        int elemental_duration_in_tc_minus1[] = new int[maxNumSubLayersMinus1];
        for (int i = 0; i <= maxNumSubLayersMinus1; i++) {
            fixed_pic_rate_general_flag[i] = r.readBool("fixed_pic_rate_general_flag[" + i + "]");
            if (!fixed_pic_rate_general_flag[i]) {
                fixed_pic_rate_within_cvs_flag[i] = r.readBool("fixed_pic_rate_within_cvs_flag[" + i + "]");
            }
            if (fixed_pic_rate_within_cvs_flag[i]) {
                elemental_duration_in_tc_minus1[i] = r.readUE("elemental_duration_in_tc_minus1[" + i + "]");
            } else {
                low_delay_hrd_flag[i] = r.readBool("low_delay_hrd_flag[" + i + "]");
            }
            if (!low_delay_hrd_flag[i]) {
                cpb_cnt_minus1[i] = r.readUE("cpb_cnt_minus1[" + i + "]");
            }
            if (nal_hrd_parameters_present_flag) {
                sub_layer_hrd_parameters(i, cpb_cnt_minus1[i], sub_pic_hrd_params_present_flag, r);
            }
            if (vcl_hrd_parameters_present_flag) {
                sub_layer_hrd_parameters(i, cpb_cnt_minus1[i], sub_pic_hrd_params_present_flag, r);
            }
        }

    }

    void sub_layer_hrd_parameters(int subLayerId, int cpbCnt, boolean sub_pic_hrd_params_present_flag, CAVLCReader r) throws IOException {
        int bit_rate_value_minus1[] = new int[cpbCnt];
        int cpb_size_value_minus1[] = new int[cpbCnt];
        int cpb_size_du_value_minus1[] = new int[cpbCnt];
        int bit_rate_du_value_minus1[] = new int[cpbCnt];
        boolean cbr_flag[] = new boolean[cpbCnt];
        for (int i = 0; i <= cpbCnt; i++) {
            bit_rate_value_minus1[i] = r.readUE("bit_rate_value_minus1[" + i + "]");
            cpb_size_value_minus1[i] = r.readUE("cpb_size_value_minus1[" + i + "]");
            if (sub_pic_hrd_params_present_flag) {
                cpb_size_du_value_minus1[i] = r.readUE("cpb_size_du_value_minus1[" + i + "]");
                bit_rate_du_value_minus1[i] = r.readUE("bit_rate_du_value_minus1[" + i + "]");
            }
            cbr_flag[i] = r.readBool("cbr_flag[" + i + "]");
        }
    }

    private List<HevcDecoderConfigurationRecord.Array> getArrays() {
        HevcDecoderConfigurationRecord.Array vpsArray = new HevcDecoderConfigurationRecord.Array();
        vpsArray.array_completeness = true;
        vpsArray.nal_unit_type = VPS_NUT;
        vpsArray.nalUnits = new ArrayList<byte[]>();
        for (ByteBuffer byteBuffer : videoParamterSets.values()) {
            byte[] ps = new byte[byteBuffer.limit()];
            byteBuffer.position(0);
            byteBuffer.get(ps);
            vpsArray.nalUnits.add(ps);
        }

        HevcDecoderConfigurationRecord.Array spsArray = new HevcDecoderConfigurationRecord.Array();
        spsArray.array_completeness = true;
        spsArray.nal_unit_type = SPS_NUT;
        spsArray.nalUnits = new ArrayList<byte[]>();
        for (ByteBuffer byteBuffer : sequenceParamterSets.values()) {
            byte[] ps = new byte[byteBuffer.limit()];
            byteBuffer.position(0);
            byteBuffer.get(ps);
            spsArray.nalUnits.add(ps);
        }

        HevcDecoderConfigurationRecord.Array ppsArray = new HevcDecoderConfigurationRecord.Array();
        ppsArray.array_completeness = true;
        ppsArray.nal_unit_type = SPS_NUT;
        ppsArray.nalUnits = new ArrayList<byte[]>();
        for (ByteBuffer byteBuffer : pictureParamterSets.values()) {
            byte[] ps = new byte[byteBuffer.limit()];
            byteBuffer.position(0);
            byteBuffer.get(ps);
            ppsArray.nalUnits.add(ps);
        }
        return Arrays.asList(vpsArray, spsArray, ppsArray);
    }

    boolean isFirstOfAU(int nalUnitType, ByteBuffer nalUnit, List<ByteBuffer> accessUnit) {
        if (accessUnit.isEmpty()) {
            return true;
        }
        boolean vclPresentInCurrentAU = getNalUnitHeader(accessUnit.get(accessUnit.size() - 1)).nalUnitType <= 31;
        switch (nalUnitType) {
            case VPS_NUT:
            case SPS_NUT:
            case PPS_NUT:
            case AUD_NUT:
            case PREFIX_SEI_NUT:
            case RSV_NVCL41:
            case RSV_NVCL42:
            case RSV_NVCL43:
            case RSV_NVCL44:
            case UNSPEC48:
            case UNSPEC49:
            case UNSPEC50:
            case UNSPEC51:
            case UNSPEC52:
            case UNSPEC53:
            case UNSPEC54:
            case UNSPEC55:
                if (vclPresentInCurrentAU) {
                    return true;
                }
        }
        switch (nalUnitType) {
            case TRAIL_N:
            case TRAIL_R:
            case TSA_N:
            case TSA_R:
            case STSA_N:
            case STSA_R:
            case RADL_N:
            case RADL_R:
            case RASL_N:
            case RASL_R:
            case BLA_W_LP:
            case BLA_W_RADL:
            case BLA_N_LP:
            case IDR_W_RADL:
            case IDR_N_LP:
            case CRA_NUT:
                byte b[] = new byte[50];
                nalUnit.position(0);
                nalUnit.get(b);
                nalUnit.position(2);
                int firstRsbp8Bit = IsoTypeReader.readUInt8(nalUnit);

                return vclPresentInCurrentAU && (firstRsbp8Bit & 0x80) > 0;
        }
        return false;
    }

    public NalUnitHeader getNalUnitHeader(ByteBuffer nal) {
        nal.position(0);
        int nal_unit_header = IsoTypeReader.readUInt16(nal);


        NalUnitHeader nalUnitHeader = new NalUnitHeader();
        nalUnitHeader.forbiddenZeroFlag = (nal_unit_header & 0x8000) >> 15;
        nalUnitHeader.nalUnitType = (nal_unit_header & 0x7E00) >> 9;
        nalUnitHeader.nuhLayerId = (nal_unit_header & 0x1F8) >> 3;
        nalUnitHeader.nuhTemporalIdPlusOne = (nal_unit_header & 0x7);
        return nalUnitHeader;
    }

    protected Sample createSample(List<ByteBuffer> nals) {
        byte[] sizeInfo = new byte[nals.size() * 4];
        ByteBuffer sizeBuf = ByteBuffer.wrap(sizeInfo);
        for (ByteBuffer b : nals) {
            sizeBuf.putInt(b.remaining());
        }

        ByteBuffer[] data = new ByteBuffer[nals.size() * 2];

        for (int i = 0; i < nals.size(); i++) {
            data[2 * i] = ByteBuffer.wrap(sizeInfo, i * 4, 4);
            data[2 * i + 1] = nals.get(i);
        }

        return new SampleImpl(data);
    }

    public enum PARSE_STATE {
        AUD_SEI_SLICE,
        SEI_SLICE,
        SLICE_OES_EOB,
    }

    public static class NalUnitHeader {
        int forbiddenZeroFlag;
        int nalUnitType;
        int nuhLayerId;
        int nuhTemporalIdPlusOne;
    }

    class LookAhead {
        long bufferStartPos = 0;
        int inBufferPos = 0;
        DataSource dataSource;
        ByteBuffer buffer;

        long start;

        LookAhead(DataSource dataSource) throws IOException {
            this.dataSource = dataSource;
            fillBuffer();
        }

        public void fillBuffer() throws IOException {
            buffer = dataSource.map(bufferStartPos, Math.min(dataSource.size() - bufferStartPos, BUFFER));
        }

        boolean nextThreeEquals001() throws IOException {
            if (buffer.limit() - inBufferPos >= 3) {
                return (buffer.get(inBufferPos) == 0 &&
                        buffer.get(inBufferPos + 1) == 0 &&
                        buffer.get(inBufferPos + 2) == 1);
            } else {
                if (bufferStartPos + inBufferPos == dataSource.size()) {
                    throw new EOFException();
                }
                throw new RuntimeException("buffer repositioning require");
            }
        }

        boolean nextThreeEquals000or001orEof() throws IOException {
            if (buffer.limit() - inBufferPos >= 3) {
                return ((buffer.get(inBufferPos) == 0 &&
                        buffer.get(inBufferPos + 1) == 0 &&
                        (buffer.get(inBufferPos + 2) == 0 || buffer.get(inBufferPos + 2) == 1)));
            } else {
                if (bufferStartPos + inBufferPos + 3 > dataSource.size()) {
                    return bufferStartPos + inBufferPos == dataSource.size();
                } else {
                    bufferStartPos = start;
                    inBufferPos = 0;
                    fillBuffer();
                    return nextThreeEquals000or001orEof();
                }
            }
        }

        void discardByte() {
            inBufferPos++;
        }

        void discardNext3AndMarkStart() {
            inBufferPos += 3;
            start = bufferStartPos + inBufferPos;
        }

        public ByteBuffer getNal() {
            if (start >= bufferStartPos) {
                buffer.position((int) (start - bufferStartPos));
                Buffer sample = buffer.slice();
                sample.limit((int) (inBufferPos - (start - bufferStartPos)));
                return (ByteBuffer) sample;
            } else {
                throw new RuntimeException("damn! NAL exceeds buffer");
                // this can only happen if NAL is bigger than the buffer
            }

        }
    }
}
