package org.mp4parser.muxer.tracks.h265;

import org.mp4parser.muxer.tracks.h264.parsing.read.CAVLCReader;

import java.io.IOException;

public class HrdParameters {
    public HrdParameters(boolean commonInfPresentFlag, int maxNumSubLayersMinus1, CAVLCReader bsr) throws IOException {
        boolean nal_hrd_parameters_present_flag = false;
        boolean vcl_hrd_parameters_present_flag = false;
        boolean sub_pic_hrd_params_present_flag = false;
        if (commonInfPresentFlag) {
            nal_hrd_parameters_present_flag = bsr.readBool("nal_hrd_parameters_present_flag");
            vcl_hrd_parameters_present_flag = bsr.readBool("vcl_hrd_parameters_present_flag");
            if (nal_hrd_parameters_present_flag || vcl_hrd_parameters_present_flag) {
                sub_pic_hrd_params_present_flag = bsr.readBool("sub_pic_hrd_params_present_flag");
                if (sub_pic_hrd_params_present_flag) {
                    int tick_divisor_minus2 = bsr.readU(8, "tick_divisor_minus2");
                    int du_cpb_removal_delay_increment_length_minus1 = bsr.readU(5, "du_cpb_removal_delay_increment_length_minus1");
                    boolean sub_pic_cpb_params_in_pic_timing_sei_flag = bsr.readBool("sub_pic_cpb_params_in_pic_timing_sei_flag");
                    int dpb_output_delay_du_length_minus1 = bsr.readU(5, "dpb_output_delay_du_length_minus1");
                }
                int bit_rate_scale = bsr.readU(4, "bit_rate_scale");
                int cpb_size_scale = bsr.readU(4, "cpb_size_scale");
                if (sub_pic_hrd_params_present_flag) {
                    int cpb_size_du_scale = bsr.readU(4, "cpb_size_du_scale");
                }
                int initial_cpb_removal_delay_length_minus1 = bsr.readU(5, "initial_cpb_removal_delay_length_minus1");
                int au_cpb_removal_delay_length_minus1 = bsr.readU(5, "au_cpb_removal_delay_length_minus1");
                int dpb_output_delay_length_minus1 = bsr.readU(5, "dpb_output_delay_length_minus1");
            }
        }
        boolean fixed_pic_rate_general_flag[] = new boolean[maxNumSubLayersMinus1 + 1];
        boolean fixed_pic_rate_within_cvs_flag[] = new boolean[maxNumSubLayersMinus1 + 1];
        int elemental_duration_in_tc_minus1[] = new int[maxNumSubLayersMinus1 + 1];
        boolean low_delay_hrd_flag[] = new boolean[maxNumSubLayersMinus1 + 1];
        int cpb_cnt_minus1[] = new int[maxNumSubLayersMinus1 + 1];

        for (int i = 0; i <= maxNumSubLayersMinus1; i++) {
            fixed_pic_rate_general_flag[i] = bsr.readBool("fixed_pic_rate_general_flag[i]");
            if (!fixed_pic_rate_general_flag[i]) {
                fixed_pic_rate_within_cvs_flag[i] = bsr.readBool("fixed_pic_rate_general_flag[i]");
            }
            if (fixed_pic_rate_within_cvs_flag[i]) {
                elemental_duration_in_tc_minus1[i] = bsr.readUE("elemental_duration_in_tc_minus1[i]");
            } else {
                low_delay_hrd_flag[i] = bsr.readBool("low_delay_hrd_flag[i]");
            }
            if (!low_delay_hrd_flag[i]) {
                cpb_cnt_minus1[i] = bsr.readUE("cpb_cnt_minus1[i]");
            }
            if (nal_hrd_parameters_present_flag) {

                new SubLayerHrdParameters(i, cpb_cnt_minus1, sub_pic_hrd_params_present_flag, bsr);
            }
            if (vcl_hrd_parameters_present_flag) {

                new SubLayerHrdParameters(i, cpb_cnt_minus1, sub_pic_hrd_params_present_flag, bsr);
            }
        }
    }
}
