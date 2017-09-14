package org.mp4parser.muxer.tracks.h265;

import org.mp4parser.muxer.tracks.h264.parsing.read.CAVLCReader;

import java.io.IOException;

public class SubLayerHrdParameters {
    public SubLayerHrdParameters(int subLayerId, int[] cpb_cnt_minus1, boolean sub_pic_hrd_params_present_flag, CAVLCReader bsr) throws IOException {

        int CpbCnt = cpb_cnt_minus1[subLayerId];
        int bit_rate_value_minus1[] = new int[CpbCnt + 1];
        int cpb_size_value_minus1[] = new int[CpbCnt + 1];
        int cpb_size_du_value_minus1[] = new int[CpbCnt + 1];
        int bit_rate_du_value_minus1[] = new int[CpbCnt + 1];
        boolean cbr_flag[] = new boolean[CpbCnt + 1];
        for (int i = 0; i <= CpbCnt; i++) {
            bit_rate_value_minus1[i] = bsr.readUE("bit_rate_value_minus1[i]");
            cpb_size_value_minus1[i] = bsr.readUE("cpb_size_value_minus1[i]");
            if (sub_pic_hrd_params_present_flag) {
                cpb_size_du_value_minus1[i] = bsr.readUE("cpb_size_du_value_minus1[i]");
                bit_rate_du_value_minus1[i] = bsr.readUE("bit_rate_du_value_minus1[i]");
            }
            cbr_flag[i] = bsr.readBool("cbr_flag[i]");
        }
    }
}
