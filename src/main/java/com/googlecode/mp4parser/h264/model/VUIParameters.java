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
