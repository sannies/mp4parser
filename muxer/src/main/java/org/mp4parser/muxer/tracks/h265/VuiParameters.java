package org.mp4parser.muxer.tracks.h265;

import org.mp4parser.muxer.tracks.h264.parsing.read.CAVLCReader;

import java.io.IOException;

public class VuiParameters {
    private static final int EXTENDED_SAR = 255;
    public boolean aspect_ratio_info_present_flag;
    public int aspect_ratio_idc;
    public int sar_width;
    public int sar_height;
    public boolean video_signal_type_present_flag;
    public int video_format;
    public boolean video_full_range_flag;
    public boolean colour_description_present_flag;
    public int colour_primaries;
    public int transfer_characteristics;
    public int matrix_coeffs;
    public boolean vui_timing_info_present_flag = false;
    public long vui_num_units_in_tick;
    public long vui_time_scale;
    public int min_spatial_segmentation_idc;


    public VuiParameters(int sps_max_sub_layers_minus1, CAVLCReader bsr) throws IOException {

        aspect_ratio_info_present_flag = bsr.readBool("aspect_ratio_info_present_flag");
        if (aspect_ratio_info_present_flag) {
            aspect_ratio_idc = bsr.readU(8, "aspect_ratio_idc");
            if (aspect_ratio_idc == EXTENDED_SAR) {
                sar_width = bsr.readU(16, "sar_width");
                sar_height = bsr.readU(16, "sar_height");
            }
        }
        boolean overscan_info_present_flag = bsr.readBool("overscan_info_present_flag");
        if (overscan_info_present_flag) {
            boolean overscan_appropriate_flag = bsr.readBool("overscan_appropriate_flag");
        }
        video_signal_type_present_flag = bsr.readBool("video_signal_type_present_flag");
        if (video_signal_type_present_flag) {
            video_format = bsr.readU(3, "video_format");
            video_full_range_flag = bsr.readBool("video_full_range_flag");
            colour_description_present_flag = bsr.readBool("colour_description_present_flag");
            if (colour_description_present_flag) {
                colour_primaries = bsr.readU(8, "colour_primaries");
                transfer_characteristics = bsr.readU(8, "transfer_characteristics");
                matrix_coeffs = bsr.readU(8, "matrix_coeffs");
            }
        }
        boolean chroma_loc_info_present_flag = bsr.readBool("chroma_loc_info_present_flag");
        if (chroma_loc_info_present_flag) {
            int chroma_sample_loc_type_top_field = bsr.readUE("chroma_sample_loc_type_top_field");
            int chroma_sample_loc_type_bottom_field = bsr.readUE("chroma_sample_loc_type_bottom_field");
        }
        boolean neutral_chroma_indication_flag = bsr.readBool("neutral_chroma_indication_flag");
        boolean field_seq_flag = bsr.readBool("field_seq_flag");
        boolean frame_field_info_present_flag = bsr.readBool("frame_field_info_present_flag");
        boolean default_display_window_flag = bsr.readBool("default_display_window_flag");
        if (default_display_window_flag) {
            int def_disp_win_left_offset = bsr.readUE("def_disp_win_left_offset");
            int def_disp_win_right_offset = bsr.readUE("def_disp_win_right_offset");
            int def_disp_win_top_offset = bsr.readUE("def_disp_win_top_offset");
            int def_disp_win_bottom_offset = bsr.readUE("def_disp_win_bottom_offset");
        }
        vui_timing_info_present_flag = bsr.readBool("vui_timing_info_present_flag");
        if (vui_timing_info_present_flag) {
            vui_num_units_in_tick = bsr.readNBit(32, "vui_num_units_in_tick");
            vui_time_scale = bsr.readNBit(32, "vui_time_scale");
            boolean vui_poc_proportional_to_timing_flag = bsr.readBool("vui_poc_proportional_to_timing_flag");
            if (vui_poc_proportional_to_timing_flag) {
                int vui_num_ticks_poc_diff_one_minus1 = bsr.readUE("vui_num_ticks_poc_diff_one_minus1");
            }
            boolean vui_hrd_parameters_present_flag = bsr.readBool("vui_hrd_parameters_present_flag");
            if (vui_hrd_parameters_present_flag) {
                new HrdParameters(true, sps_max_sub_layers_minus1, bsr);
            }
        }
        boolean bitstream_restriction_flag = bsr.readBool("bitstream_restriction_flag");
        if (bitstream_restriction_flag) {
            boolean tiles_fixed_structure_flag = bsr.readBool("tiles_fixed_structure_flag");
            boolean motion_vectors_over_pic_boundaries_flag = bsr.readBool("motion_vectors_over_pic_boundaries_flag");
            boolean restricted_ref_pic_lists_flag = bsr.readBool("restricted_ref_pic_lists_flag");
            min_spatial_segmentation_idc = bsr.readUE("min_spatial_segmentation_idc");
            int max_bytes_per_pic_denom = bsr.readUE("max_bytes_per_pic_denom");
            int max_bits_per_min_cu_denom = bsr.readUE("max_bits_per_min_cu_denom");
            int log2_max_mv_length_horizontal = bsr.readUE("log2_max_mv_length_horizontal");
            int log2_max_mv_length_vertical = bsr.readUE("log2_max_mv_length_vertical");
        }
    }

}
