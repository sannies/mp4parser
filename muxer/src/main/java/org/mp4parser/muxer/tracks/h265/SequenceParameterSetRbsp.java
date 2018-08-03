package org.mp4parser.muxer.tracks.h265;

import org.mp4parser.muxer.tracks.h264.parsing.read.CAVLCReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sannies on 03.02.2015.
 */
public class SequenceParameterSetRbsp {
    public VuiParameters vuiParameters;


    public SequenceParameterSetRbsp(InputStream is) throws IOException {
        CAVLCReader bsr = new CAVLCReader(is);

        int sps_video_parameter_set_id = (int) bsr.readNBit(4, "sps_video_parameter_set_id");
        int sps_max_sub_layers_minus1 = (int) bsr.readNBit(3, "sps_max_sub_layers_minus1");
        boolean sps_temporal_id_nesting_flag = bsr.readBool("sps_temporal_id_nesting_flag");
        profile_tier_level(sps_max_sub_layers_minus1, bsr);
        int sps_seq_parameter_set_id = bsr.readUE("sps_seq_parameter_set_id");
        int chroma_format_idc = bsr.readUE("chroma_format_idc");
        if (chroma_format_idc == 3) {
            int separate_colour_plane_flag = bsr.read1Bit();
        }
        int pic_width_in_luma_samples = bsr.readUE("pic_width_in_luma_samples");
        int pic_height_in_luma_samples = bsr.readUE("pic_width_in_luma_samples");
        boolean conformance_window_flag = bsr.readBool("conformance_window_flag");
        if (conformance_window_flag) {
            int conf_win_left_offset = bsr.readUE("conf_win_left_offset");
            int conf_win_right_offset = bsr.readUE("conf_win_right_offset");
            int conf_win_top_offset = bsr.readUE("conf_win_top_offset");
            int conf_win_bottom_offset = bsr.readUE("conf_win_bottom_offset");
        }

        int bit_depth_luma_minus8 = bsr.readUE("bit_depth_luma_minus8");
        int bit_depth_chroma_minus8 = bsr.readUE("bit_depth_chroma_minus8");
        int log2_max_pic_order_cnt_lsb_minus4 = bsr.readUE("log2_max_pic_order_cnt_lsb_minus4");
        boolean sps_sub_layer_ordering_info_present_flag = bsr.readBool("sps_sub_layer_ordering_info_present_flag");

        int j = sps_max_sub_layers_minus1 - (sps_sub_layer_ordering_info_present_flag ? 0 : sps_max_sub_layers_minus1) + 1;
        int sps_max_dec_pic_buffering_minus1[] = new int[j];
        int sps_max_num_reorder_pics[] = new int[j];
        int sps_max_latency_increase_plus1[] = new int[j];

        for (int i = (sps_sub_layer_ordering_info_present_flag ? 0 : sps_max_sub_layers_minus1); i <= sps_max_sub_layers_minus1; i++) {
            sps_max_dec_pic_buffering_minus1[i] = bsr.readUE("sps_max_dec_pic_buffering_minus1[" + i + "]");
            sps_max_num_reorder_pics[i] = bsr.readUE("sps_max_num_reorder_pics[" + i + "]");
            sps_max_latency_increase_plus1[i] = bsr.readUE("sps_max_latency_increase_plus1[" + i + "]");
        }

        int log2_min_luma_coding_block_size_minus3 = bsr.readUE("log2_min_luma_coding_block_size_minus3");
        int log2_diff_max_min_luma_coding_block_size = bsr.readUE("log2_diff_max_min_luma_coding_block_size");
        int log2_min_transform_block_size_minus2 = bsr.readUE("log2_min_transform_block_size_minus2");
        int log2_diff_max_min_transform_block_size = bsr.readUE("log2_diff_max_min_transform_block_size");
        int max_transform_hierarchy_depth_inter = bsr.readUE("max_transform_hierarchy_depth_inter");
        int max_transform_hierarchy_depth_intra = bsr.readUE("max_transform_hierarchy_depth_intra");

        boolean scaling_list_enabled_flag = bsr.readBool("scaling_list_enabled_flag");
        if (scaling_list_enabled_flag) {
            boolean sps_scaling_list_data_present_flag = bsr.readBool("sps_scaling_list_data_present_flag");
            if (sps_scaling_list_data_present_flag) {
                scaling_list_data(bsr);
            }
        }
        boolean amp_enabled_flag = bsr.readBool("amp_enabled_flag");
        boolean sample_adaptive_offset_enabled_flag = bsr.readBool("sample_adaptive_offset_enabled_flag");
        boolean pcm_enabled_flag = bsr.readBool("pcm_enabled_flag");

        if (pcm_enabled_flag) {
            int pcm_sample_bit_depth_luma_minus1 = (int) bsr.readNBit(4, "pcm_sample_bit_depth_luma_minus1");
            int pcm_sample_bit_depth_chroma_minus1 = (int) bsr.readNBit(4, "pcm_sample_bit_depth_chroma_minus1");
            int log2_min_pcm_luma_coding_block_size_minus3 = bsr.readUE("log2_min_pcm_luma_coding_block_size_minus3");
            int log2_diff_max_min_pcm_luma_coding_block_size = bsr.readUE("log2_diff_max_min_pcm_luma_coding_block_size");
            boolean pcm_loop_filter_disabled_flag = bsr.readBool("pcm_loop_filter_disabled_flag");
        }
        int num_short_term_ref_pic_sets = bsr.readUE("num_short_term_ref_pic_sets");
        for (int i = 0; i < num_short_term_ref_pic_sets; i++) {
            short_term_ref_pic_set(i, num_short_term_ref_pic_sets, bsr);
        }
        boolean long_term_ref_pics_present_flag = bsr.readBool("long_term_ref_pics_present_flag");
        if (long_term_ref_pics_present_flag) {
            int num_long_term_ref_pics_sps = bsr.readUE("num_long_term_ref_pics_sps");
            int lt_ref_pic_poc_lsb_sps[] = new int[num_long_term_ref_pics_sps];
            boolean used_by_curr_pic_lt_sps_flag[] = new boolean[num_long_term_ref_pics_sps];
            for (int i = 0; i < num_long_term_ref_pics_sps; i++) {
                lt_ref_pic_poc_lsb_sps[i] = bsr.readU(log2_max_pic_order_cnt_lsb_minus4 + 4, "lt_ref_pic_poc_lsb_sps[" + i + "]");
                used_by_curr_pic_lt_sps_flag[i] = bsr.readBool("used_by_curr_pic_lt_sps_flag[" + i + "]");
            }
        }
        boolean sps_temporal_mvp_enabled_flag = bsr.readBool("sps_temporal_mvp_enabled_flag");
        boolean strong_intra_smoothing_enabled_flag = bsr.readBool("strong_intra_smoothing_enabled_flag");
        boolean vui_parameters_present_flag = bsr.readBool("vui_parameters_present_flag");
        if (vui_parameters_present_flag) {
            vuiParameters = new VuiParameters(sps_max_sub_layers_minus1, bsr);
        }
    }



    private static void short_term_ref_pic_set(int stRpsIdx, int num_short_term_ref_pic_sets, CAVLCReader bsr) throws IOException {
      /*  boolean inter_ref_pic_set_prediction_flag = false;
        if (stRpsIdx != 0)
            inter_ref_pic_set_prediction_flag = bsr.readBool("inter_ref_pic_set_prediction_flag");
        if (inter_ref_pic_set_prediction_flag) {
            if (stRpsIdx == num_short_term_ref_pic_sets) {
                int delta_idx_minus1 = bsr.readUE("delta_idx_minus1");
            }
            int delta_rps_sign = bsr.read1Bit();
            int abs_delta_rps_minus1 = bsr.readUE("abs_delta_rps_minus1");
            boolean used_by_curr_pic_flag[] = new boolean[NumDeltaPocs[RefRpsIdx] + 1];
            boolean use_delta_flag[] = new boolean[NumDeltaPocs[RefRpsIdx] + 1];
            for (int j = 0; j <= NumDeltaPocs[RefRpsIdx]; j++) {
                used_by_curr_pic_flag[j] =bsr.readBool("used_by_curr_pic_flag[" + j + "]");
                if (!used_by_curr_pic_flag[j])
                    use_delta_flag[j]=bsr.readBool("use_delta_flag[" + j + "]");
            }
        } else {
            int num_negative_pics = bsr.readUE ("num_negative_pics");
            int num_positive_pics = bsr.readUE ("num_positive_pics");
            int delta_poc_s0_minus1[] = new int[num_negative_pics];
            boolean used_by_curr_pic_s0_flag[] = new boolean[num_negative_pics];
            for (int i = 0; i < num_negative_pics; i++) {
                delta_poc_s0_minus1[i] = bsr.readUE("delta_poc_s0_minus1[" + i + "]");
                used_by_curr_pic_s0_flag[i] = bsr.readBool("used_by_curr_pic_s0_flag[" + i +"]");
            }
            int delta_poc_s1_minus1[] = new int[num_positive_pics];
            boolean used_by_curr_pic_s1_flag[] = new boolean[num_positive_pics];
            for (int i = 0; i < num_positive_pics; i++) {
                delta_poc_s1_minus1[i] = bsr.readUE("delta_poc_s1_minus1[" + i + "]");
                used_by_curr_pic_s1_flag[i] = bsr.readBool("used_by_curr_pic_s1_flag[" + i +"]");
            }
        }*/
        throw new RuntimeException("short_term_ref_pic_set not implemented");
    }

    private static void scaling_list_data(CAVLCReader bsr) throws IOException {
        boolean[][] scaling_list_pred_mode_flag = new boolean[4][];
        int[][] scaling_list_pred_matrix_id_delta = new int[4][];
        int[][] scaling_list_dc_coef_minus8 = new int[2][];
        int[][][] ScalingList = new int[4][][];

        for (int sizeId = 0; sizeId < 4; sizeId++) {
            for (int matrixId = 0; matrixId < ((sizeId == 3) ? 2 : 6); matrixId++) {
                scaling_list_pred_mode_flag[sizeId] = new boolean[((sizeId == 3) ? 2 : 6)];
                scaling_list_pred_matrix_id_delta[sizeId] = new int[((sizeId == 3) ? 2 : 6)];
                ScalingList[sizeId] = new int[(sizeId == 3) ? 2 : 6][];
                scaling_list_pred_mode_flag[sizeId][matrixId] = bsr.readBool();
                if (!scaling_list_pred_mode_flag[sizeId][matrixId]) {
                    scaling_list_pred_matrix_id_delta[sizeId][matrixId] = bsr.readUE("scaling_list_pred_matrix_id_delta[" + sizeId + "][" + matrixId + "]");
                } else {
                    int nextCoef = 8;
                    int coefNum = Math.min(64, (1 << (4 + (sizeId << 1))));
                    if (sizeId > 1) {
                        scaling_list_dc_coef_minus8[sizeId - 2][matrixId] = bsr.readSE("scaling_list_dc_coef_minus8[" + sizeId + "- 2][" + matrixId + "]");
                        nextCoef = scaling_list_dc_coef_minus8[sizeId - 2][matrixId] + 8;
                    }
                    ScalingList[sizeId][matrixId] = new int[coefNum];
                    for (int i = 0; i < coefNum; i++) {
                        int scaling_list_delta_coef = bsr.readSE("scaling_list_delta_coef ");
                        nextCoef = (nextCoef + scaling_list_delta_coef + 256) % 256;
                        ScalingList[sizeId][matrixId][i] = nextCoef;
                    }
                }
            }
        }
    }


    private static void profile_tier_level(int maxNumSubLayersMinus1, CAVLCReader bsr) throws IOException {
        int general_profile_space = bsr.readU(2, "general_profile_space");
        boolean general_tier_flag = bsr.readBool("general_tier_flag");
        int general_profile_idc = bsr.readU(5, "general_profile_idc");
        boolean general_profile_compatibility_flag[] = new boolean[32];
        for (int j = 0; j < 32; j++) {
            general_profile_compatibility_flag[j] = bsr.readBool();
        }
        boolean general_progressive_source_flag = bsr.readBool("general_progressive_source_flag");
        boolean general_interlaced_source_flag = bsr.readBool("general_interlaced_source_flag");
        boolean general_non_packed_constraint_flag = bsr.readBool("general_non_packed_constraint_flag");
        boolean general_frame_only_constraint_flag = bsr.readBool("general_frame_only_constraint_flag");
        long general_reserved_zero_44bits = bsr.readNBit(44, "general_reserved_zero_44bits");
        int general_level_idc = bsr.readByte();
        boolean[] sub_layer_profile_present_flag = new boolean[maxNumSubLayersMinus1];
        boolean[] sub_layer_level_present_flag = new boolean[maxNumSubLayersMinus1];
        for (int i = 0; i < maxNumSubLayersMinus1; i++) {
            sub_layer_profile_present_flag[i] = bsr.readBool("sub_layer_profile_present_flag[" + i + "]");
            sub_layer_level_present_flag[i] = bsr.readBool("sub_layer_level_present_flag[" + i + "]");
        }

        if (maxNumSubLayersMinus1 > 0) {
            int[] reserved_zero_2bits = new int[8];

            for (int i = maxNumSubLayersMinus1; i < 8; i++) {
                reserved_zero_2bits[i] = bsr.readU(2, "reserved_zero_2bits[" + i + "]");
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
        long[] sub_layer_reserved_zero_44bits = new long[maxNumSubLayersMinus1];
        int[] sub_layer_level_idc = new int[maxNumSubLayersMinus1];


        for (int i = 0; i < maxNumSubLayersMinus1; i++) {
            if (sub_layer_profile_present_flag[i]) {
                sub_layer_profile_space[i] = bsr.readU(2, "sub_layer_profile_space[" + i + "]");
                sub_layer_tier_flag[i] = bsr.readBool("sub_layer_tier_flag[" + i + "]");
                sub_layer_profile_idc[i] = bsr.readU(5, "sub_layer_profile_idc[" + i + "]");
                for (int j = 0; j < 32; j++) {
                    sub_layer_profile_compatibility_flag[i][j] = bsr.readBool("sub_layer_profile_compatibility_flag[" + i + "][" + j + "]");
                }
                sub_layer_progressive_source_flag[i] = bsr.readBool("sub_layer_progressive_source_flag[" + i + "]");
                sub_layer_interlaced_source_flag[i] = bsr.readBool("sub_layer_interlaced_source_flag[" + i + "]");
                sub_layer_non_packed_constraint_flag[i] = bsr.readBool("sub_layer_non_packed_constraint_flag[" + i + "]");
                sub_layer_frame_only_constraint_flag[i] = bsr.readBool("sub_layer_frame_only_constraint_flag[" + i + "]");
                sub_layer_reserved_zero_44bits[i] = bsr.readNBit(44);
            }
            if (sub_layer_level_present_flag[i]) {
                sub_layer_level_idc[i] = bsr.readU(8, "sub_layer_level_idc[" + i + "]");
            }
        }
    }
}
