/* Copyright */
package com.googlecode.mp4parser.h264.model;

public class VUIParameters {

    public static class BitstreamRestriction {

        public boolean motion_vectors_over_pic_boundaries_flag;
        public int max_bytes_per_pic_denom;
        public int max_bits_per_mb_denom;
        public int log2_max_mv_length_horizontal;
        public int log2_max_mv_length_vertical;
        public int num_reorder_frames;
        public int max_dec_frame_buffering;

    }

    public boolean aspect_ratio_info_present_flag;
    public int sar_width;
    public int sar_height;
    public boolean overscan_info_present_flag;
    public boolean overscan_appropriate_flag;
    public boolean video_signal_type_present_flag;
    public int video_format;
    public boolean video_full_range_flag;
    public boolean colour_description_present_flag;
    public int colour_primaries;
    public int transfer_characteristics;
    public int matrix_coefficients;
    public boolean chroma_loc_info_present_flag;
    public int chroma_sample_loc_type_top_field;
    public int chroma_sample_loc_type_bottom_field;
    public boolean timing_info_present_flag;
    public int num_units_in_tick;
    public int time_scale;
    public boolean fixed_frame_rate_flag;
    public boolean low_delay_hrd_flag;
    public boolean pic_struct_present_flag;
    public HRDParameters nalHRDParams;
    public HRDParameters vclHRDParams;

    public BitstreamRestriction bitstreamRestriction;
    public AspectRatio aspect_ratio;

}
