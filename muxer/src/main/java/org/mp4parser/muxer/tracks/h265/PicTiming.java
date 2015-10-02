package org.mp4parser.muxer.tracks.h265;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sannies on 03.02.2015.
 */
public class PicTiming {

    PicTiming(int payloadSize, InputStream is) throws IOException {
       /* if (frame_field_info_present_flag) {
            int pic_struct = (int) bsr.readNBit(4);
            int source_scan_type = (int) bsr.readNBit(2);
            boolean duplicate_flag = bsr.read1Bit() == 1;
        }
        if (CpbDpbDelaysPresentFlag) {
            au_cpb_removal_delay_minus1 = brb.u(v)
                    pic_dpb_output_delay u(v)
            if (sub_pic_hrd_params_present_flag)
                pic_dpb_output_du_delay u (v)
            if (sub_pic_hrd_params_present_flag &&
                    sub_pic_cpb_params_in_pic_timing_sei_flag) {
                num_decoding_units_minus1 ue (v)
                        du_common_cpb_removal_delay_flag u(1) if (du_common_cpb_removal_delay_flag)
                    du_common_cpb_removal_delay_increment_minus1 u (v)
                for (i = 0; i <= num_decoding_units_minus1; i++) {
                    num_nalus_in_du_minus1[i] ue(v)
                    if (!du_common_cpb_removal_delay_flag && i < num_decoding_units_minus1)
                        du_cpb_removal_delay_increment_minus1[i] u(v)
                }
            }
        }*/
    }
}
