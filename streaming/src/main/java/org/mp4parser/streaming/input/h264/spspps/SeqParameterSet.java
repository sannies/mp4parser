/*
Copyright (c) 2011 Stanislav Vitvitskiy

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.mp4parser.streaming.input.h264.spspps;


import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Sequence Parameter Set structure of h264 bitstream
 * <p>
 * capable to serialize and deserialize with CAVLC bitstream</p>
 *
 * @author Stanislav Vitvitskiy
 */
public class SeqParameterSet {
    public int pic_order_cnt_type;
    public boolean field_pic_flag;
    public boolean delta_pic_order_always_zero_flag;
    public boolean weighted_pred_flag;
    public int weighted_bipred_idc;
    public boolean entropy_coding_mode_flag;
    public boolean mb_adaptive_frame_field_flag;
    public boolean direct_8x8_inference_flag;
    public ChromaFormat chroma_format_idc;
    public int log2_max_frame_num_minus4;
    public int log2_max_pic_order_cnt_lsb_minus4;
    public int pic_height_in_map_units_minus1;
    public int pic_width_in_mbs_minus1;
    public int bit_depth_luma_minus8;
    public int bit_depth_chroma_minus8;
    public boolean qpprime_y_zero_transform_bypass_flag;
    public int profile_idc;
    public long reserved_zero_2bits;
    public boolean constraint_set_0_flag;
    public boolean constraint_set_1_flag;
    public boolean constraint_set_2_flag;
    public boolean constraint_set_3_flag;
    public boolean constraint_set_4_flag;
    public boolean constraint_set_5_flag;
    public int level_idc;
    public int seq_parameter_set_id;
    public boolean residual_color_transform_flag;
    public int offset_for_non_ref_pic;
    public int offset_for_top_to_bottom_field;
    public int num_ref_frames;
    public boolean gaps_in_frame_num_value_allowed_flag;
    public boolean frame_mbs_only_flag;
    public boolean frame_cropping_flag;
    public int frame_crop_left_offset;
    public int frame_crop_right_offset;
    public int frame_crop_top_offset;
    public int frame_crop_bottom_offset;
    public int[] offsetForRefFrame;
    public VUIParameters vuiParams;
    public ScalingMatrix scalingMatrix;
    public int num_ref_frames_in_pic_order_cnt_cycle;


    public static SeqParameterSet read(ByteBuffer b) throws IOException {
        ByteBufferBitreader reader = new ByteBufferBitreader(b);
        SeqParameterSet sps = new SeqParameterSet();

        sps.profile_idc = (int) reader.readNBit(8);
        sps.constraint_set_0_flag = reader
                .readBool();
        sps.constraint_set_1_flag = reader
                .readBool();
        sps.constraint_set_2_flag = reader
                .readBool();
        sps.constraint_set_3_flag = reader
                .readBool();
        sps.constraint_set_4_flag = reader
                .readBool();
        sps.constraint_set_5_flag = reader
                .readBool();

        sps.reserved_zero_2bits = reader.readNBit(2);
        sps.level_idc = (int) reader.readNBit(8);
        sps.seq_parameter_set_id = reader.readUE();

        if (sps.profile_idc == 100 || sps.profile_idc == 110
                || sps.profile_idc == 122 || sps.profile_idc == 144) {
            sps.chroma_format_idc = ChromaFormat.fromId(reader
                    .readUE());
            if (sps.chroma_format_idc == ChromaFormat.YUV_444) {
                sps.residual_color_transform_flag = reader
                        .readBool();
            }
            sps.bit_depth_luma_minus8 = reader
                    .readUE();
            sps.bit_depth_chroma_minus8 = reader
                    .readUE();
            sps.qpprime_y_zero_transform_bypass_flag = reader
                    .readBool();
            boolean seqScalingMatrixPresent = reader
                    .readBool();
            if (seqScalingMatrixPresent) {
                readScalingListMatrix(reader, sps);
            }
        } else {
            sps.chroma_format_idc = ChromaFormat.YUV_420;
        }
        sps.log2_max_frame_num_minus4 = reader
                .readUE();
        sps.pic_order_cnt_type = reader.readUE();
        if (sps.pic_order_cnt_type == 0) {
            sps.log2_max_pic_order_cnt_lsb_minus4 = reader
                    .readUE();
        } else if (sps.pic_order_cnt_type == 1) {
            sps.delta_pic_order_always_zero_flag = reader
                    .readBool();
            sps.offset_for_non_ref_pic = reader
                    .readSE();
            sps.offset_for_top_to_bottom_field = reader
                    .readSE();
            sps.num_ref_frames_in_pic_order_cnt_cycle = reader
                    .readUE();
            sps.offsetForRefFrame = new int[sps.num_ref_frames_in_pic_order_cnt_cycle];
            for (int i = 0; i < sps.num_ref_frames_in_pic_order_cnt_cycle; i++) {
                sps.offsetForRefFrame[i] = reader
                        .readSE();
            }
        }
        sps.num_ref_frames = reader.readUE();
        sps.gaps_in_frame_num_value_allowed_flag = reader
                .readBool();
        sps.pic_width_in_mbs_minus1 = reader
                .readUE();
        sps.pic_height_in_map_units_minus1 = reader
                .readUE();
        sps.frame_mbs_only_flag = reader.readBool();
        if (!sps.frame_mbs_only_flag) {
            sps.mb_adaptive_frame_field_flag = reader
                    .readBool();
        }
        sps.direct_8x8_inference_flag = reader
                .readBool();
        sps.frame_cropping_flag = reader.readBool();
        if (sps.frame_cropping_flag) {
            sps.frame_crop_left_offset = reader
                    .readUE();
            sps.frame_crop_right_offset = reader
                    .readUE();
            sps.frame_crop_top_offset = reader
                    .readUE();
            sps.frame_crop_bottom_offset = reader
                    .readUE();
        }
        boolean vui_parameters_present_flag = reader
                .readBool();
        if (vui_parameters_present_flag)
            sps.vuiParams = ReadVUIParameters(reader);

        // reader.readTrailingBits();

        return sps;
    }

    private static void readScalingListMatrix(ByteBufferBitreader reader,
                                              SeqParameterSet sps) throws IOException {
        sps.scalingMatrix = new ScalingMatrix();
        for (int i = 0; i < 8; i++) {
            boolean seqScalingListPresentFlag = reader
                    .readBool();
            if (seqScalingListPresentFlag) {
                sps.scalingMatrix.ScalingList4x4 = new ScalingList[8];
                sps.scalingMatrix.ScalingList8x8 = new ScalingList[8];
                if (i < 6) {
                    sps.scalingMatrix.ScalingList4x4[i] = ScalingList.read(
                            reader, 16);
                } else {
                    sps.scalingMatrix.ScalingList8x8[i - 6] = ScalingList.read(
                            reader, 64);
                }
            }
        }
    }

    private static VUIParameters ReadVUIParameters(ByteBufferBitreader reader)
            throws IOException {
        VUIParameters vuip = new VUIParameters();
        vuip.aspect_ratio_info_present_flag = reader
                .readBool();
        if (vuip.aspect_ratio_info_present_flag) {
            vuip.aspect_ratio = AspectRatio.fromValue((int) reader.readNBit(8));
            if (vuip.aspect_ratio == AspectRatio.Extended_SAR) {
                vuip.sar_width = (int) reader.readNBit(16);
                vuip.sar_height = (int) reader.readNBit(16);
            }
        }
        vuip.overscan_info_present_flag = reader
                .readBool();
        if (vuip.overscan_info_present_flag) {
            vuip.overscan_appropriate_flag = reader
                    .readBool();
        }
        vuip.video_signal_type_present_flag = reader
                .readBool();
        if (vuip.video_signal_type_present_flag) {
            vuip.video_format = (int) reader.readNBit(3);
            vuip.video_full_range_flag = reader
                    .readBool();
            vuip.colour_description_present_flag = reader
                    .readBool();
            if (vuip.colour_description_present_flag) {
                vuip.colour_primaries = (int) reader.readNBit(8);
                vuip.transfer_characteristics = (int) reader.readNBit(8);
                vuip.matrix_coefficients = (int) reader.readNBit(8);
            }
        }
        vuip.chroma_loc_info_present_flag = reader
                .readBool();
        if (vuip.chroma_loc_info_present_flag) {
            vuip.chroma_sample_loc_type_top_field = reader
                    .readUE();
            vuip.chroma_sample_loc_type_bottom_field = reader
                    .readUE();
        }
        vuip.timing_info_present_flag = reader
                .readBool();
        if (vuip.timing_info_present_flag) {
            vuip.num_units_in_tick = (int) reader.readNBit(32);
            vuip.time_scale = (int) reader.readNBit(32);
            vuip.fixed_frame_rate_flag = reader
                    .readBool();
        }
        boolean nal_hrd_parameters_present_flag = reader
                .readBool();
        if (nal_hrd_parameters_present_flag)
            vuip.nalHRDParams = readHRDParameters(reader);
        boolean vcl_hrd_parameters_present_flag = reader
                .readBool();
        if (vcl_hrd_parameters_present_flag)
            vuip.vclHRDParams = readHRDParameters(reader);
        if (nal_hrd_parameters_present_flag || vcl_hrd_parameters_present_flag) {
            vuip.low_delay_hrd_flag = reader
                    .readBool();
        }
        vuip.pic_struct_present_flag = reader
                .readBool();
        boolean bitstream_restriction_flag = reader
                .readBool();
        if (bitstream_restriction_flag) {
            vuip.bitstreamRestriction = new VUIParameters.BitstreamRestriction();
            vuip.bitstreamRestriction.motion_vectors_over_pic_boundaries_flag = reader
                    .readBool();
            vuip.bitstreamRestriction.max_bytes_per_pic_denom = reader
                    .readUE();
            vuip.bitstreamRestriction.max_bits_per_mb_denom = reader
                    .readUE();
            vuip.bitstreamRestriction.log2_max_mv_length_horizontal = reader
                    .readUE();
            vuip.bitstreamRestriction.log2_max_mv_length_vertical = reader
                    .readUE();
            vuip.bitstreamRestriction.num_reorder_frames = reader
                    .readUE();
            vuip.bitstreamRestriction.max_dec_frame_buffering = reader
                    .readUE();
        }

        return vuip;
    }

    private static HRDParameters readHRDParameters(ByteBufferBitreader reader)
            throws IOException {
        HRDParameters hrd = new HRDParameters();
        hrd.cpb_cnt_minus1 = reader.readUE();
        hrd.bit_rate_scale = (int) reader.readNBit(4);
        hrd.cpb_size_scale = (int) reader.readNBit(4);
        hrd.bit_rate_value_minus1 = new int[hrd.cpb_cnt_minus1 + 1];
        hrd.cpb_size_value_minus1 = new int[hrd.cpb_cnt_minus1 + 1];
        hrd.cbr_flag = new boolean[hrd.cpb_cnt_minus1 + 1];

        for (int SchedSelIdx = 0; SchedSelIdx <= hrd.cpb_cnt_minus1; SchedSelIdx++) {
            hrd.bit_rate_value_minus1[SchedSelIdx] = reader
                    .readUE();
            hrd.cpb_size_value_minus1[SchedSelIdx] = reader
                    .readUE();
            hrd.cbr_flag[SchedSelIdx] = reader.readBool();
        }
        hrd.initial_cpb_removal_delay_length_minus1 = (int) reader.readNBit(5);
        hrd.cpb_removal_delay_length_minus1 = (int) reader.readNBit(5);
        hrd.dpb_output_delay_length_minus1 = (int) reader.readNBit(5);
        hrd.time_offset_length = (int) reader.readNBit(5);
        return hrd;
    }

    @Override
    public String toString() {
        return "SeqParameterSet{ " +
                "\n        pic_order_cnt_type=" + pic_order_cnt_type +
                ", \n        field_pic_flag=" + field_pic_flag +
                ", \n        delta_pic_order_always_zero_flag=" + delta_pic_order_always_zero_flag +
                ", \n        weighted_pred_flag=" + weighted_pred_flag +
                ", \n        weighted_bipred_idc=" + weighted_bipred_idc +
                ", \n        entropy_coding_mode_flag=" + entropy_coding_mode_flag +
                ", \n        mb_adaptive_frame_field_flag=" + mb_adaptive_frame_field_flag +
                ", \n        direct_8x8_inference_flag=" + direct_8x8_inference_flag +
                ", \n        chroma_format_idc=" + chroma_format_idc +
                ", \n        log2_max_frame_num_minus4=" + log2_max_frame_num_minus4 +
                ", \n        log2_max_pic_order_cnt_lsb_minus4=" + log2_max_pic_order_cnt_lsb_minus4 +
                ", \n        pic_height_in_map_units_minus1=" + pic_height_in_map_units_minus1 +
                ", \n        pic_width_in_mbs_minus1=" + pic_width_in_mbs_minus1 +
                ", \n        bit_depth_luma_minus8=" + bit_depth_luma_minus8 +
                ", \n        bit_depth_chroma_minus8=" + bit_depth_chroma_minus8 +
                ", \n        qpprime_y_zero_transform_bypass_flag=" + qpprime_y_zero_transform_bypass_flag +
                ", \n        profile_idc=" + profile_idc +
                ", \n        constraint_set_0_flag=" + constraint_set_0_flag +
                ", \n        constraint_set_1_flag=" + constraint_set_1_flag +
                ", \n        constraint_set_2_flag=" + constraint_set_2_flag +
                ", \n        constraint_set_3_flag=" + constraint_set_3_flag +
                ", \n        constraint_set_4_flag=" + constraint_set_4_flag +
                ", \n        constraint_set_5_flag=" + constraint_set_5_flag +
                ", \n        level_idc=" + level_idc +
                ", \n        seq_parameter_set_id=" + seq_parameter_set_id +
                ", \n        residual_color_transform_flag=" + residual_color_transform_flag +
                ", \n        offset_for_non_ref_pic=" + offset_for_non_ref_pic +
                ", \n        offset_for_top_to_bottom_field=" + offset_for_top_to_bottom_field +
                ", \n        num_ref_frames=" + num_ref_frames +
                ", \n        gaps_in_frame_num_value_allowed_flag=" + gaps_in_frame_num_value_allowed_flag +
                ", \n        frame_mbs_only_flag=" + frame_mbs_only_flag +
                ", \n        frame_cropping_flag=" + frame_cropping_flag +
                ", \n        frame_crop_left_offset=" + frame_crop_left_offset +
                ", \n        frame_crop_right_offset=" + frame_crop_right_offset +
                ", \n        frame_crop_top_offset=" + frame_crop_top_offset +
                ", \n        frame_crop_bottom_offset=" + frame_crop_bottom_offset +
                ", \n        offsetForRefFrame=" + offsetForRefFrame +
                ", \n        vuiParams=" + vuiParams +
                ", \n        scalingMatrix=" + scalingMatrix +
                ", \n        num_ref_frames_in_pic_order_cnt_cycle=" + num_ref_frames_in_pic_order_cnt_cycle +
                '}';
    }
}