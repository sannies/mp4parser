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
package com.googlecode.mp4parser.h264.read;

import com.googlecode.mp4parser.h264.StreamParams;
import com.googlecode.mp4parser.h264.model.ChromaFormat;
import com.googlecode.mp4parser.h264.model.NALUnit;
import com.googlecode.mp4parser.h264.model.NALUnitType;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.PredictionWeightTable;
import com.googlecode.mp4parser.h264.model.RefPicMarking;
import com.googlecode.mp4parser.h264.model.RefPicMarkingIDR;
import com.googlecode.mp4parser.h264.model.RefPicReordering;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;
import com.googlecode.mp4parser.h264.model.SliceHeader;
import com.googlecode.mp4parser.h264.model.SliceType;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Reads header of the coded slice
 *
 * @author Stanislav Vitvitskiy
 */
public class SliceHeaderReader {
    private StreamParams streamParams;

    public SliceHeaderReader(StreamParams demuxer) {
        this.streamParams = demuxer;
    }

    public SliceHeader read(NALUnit nalUnit, CAVLCReader reader)
            throws IOException {

        SliceHeader sh = new SliceHeader();
        sh.first_mb_in_slice = reader.readUE("SH: first_mb_in_slice");
        int sh_type = reader.readUE("SH: slice_type");
        sh.slice_type = SliceType.fromValue(sh_type % 5);
        sh.slice_type_restr = (sh_type / 5) > 0;

        sh.pic_parameter_set_id = reader.readUE("SH: pic_parameter_set_id");

        PictureParameterSet pps = streamParams.getPPS(sh.pic_parameter_set_id);
        SeqParameterSet sps = streamParams.getSPS(pps.seq_parameter_set_id);

        sh.pps = pps;
        sh.sps = sps;

        sh.frame_num = reader.readU(sps.log2_max_frame_num_minus4 + 4,
                "SH: frame_num");
        if (!sps.frame_mbs_only_flag) {
            sh.field_pic_flag = reader.readBool("SH: field_pic_flag");
            if (sh.field_pic_flag) {
                sh.bottom_field_flag = reader.readBool("SH: bottom_field_flag");
            }
        }
        if (nalUnit.type == NALUnitType.IDR_SLICE) {
            sh.idr_pic_id = reader.readUE("SH: idr_pic_id");
        }
        if (sps.pic_order_cnt_type == 0) {
            sh.pic_order_cnt_lsb = reader.readU(
                    sps.log2_max_pic_order_cnt_lsb_minus4 + 4,
                    "SH: pic_order_cnt_lsb");
            if (pps.pic_order_present_flag && !sps.field_pic_flag) {
                sh.delta_pic_order_cnt_bottom = reader
                        .readSE("SH: delta_pic_order_cnt_bottom");
            }
        }
        sh.delta_pic_order_cnt = new int[2];
        if (sps.pic_order_cnt_type == 1
                && !sps.delta_pic_order_always_zero_flag) {
            sh.delta_pic_order_cnt[0] = reader
                    .readSE("SH: delta_pic_order_cnt[0]");
            if (pps.pic_order_present_flag && !sps.field_pic_flag)
                sh.delta_pic_order_cnt[1] = reader
                        .readSE("SH: delta_pic_order_cnt[1]");
        }
        if (pps.redundant_pic_cnt_present_flag) {
            sh.redundant_pic_cnt = reader.readUE("SH: redundant_pic_cnt");
        }
        if (sh.slice_type == SliceType.B) {
            sh.direct_spatial_mv_pred_flag = reader
                    .readBool("SH: direct_spatial_mv_pred_flag");
        }
        if (sh.slice_type == SliceType.P || sh.slice_type == SliceType.SP
                || sh.slice_type == SliceType.B) {
            sh.num_ref_idx_active_override_flag = reader
                    .readBool("SH: num_ref_idx_active_override_flag");
            if (sh.num_ref_idx_active_override_flag) {
                sh.num_ref_idx_l0_active_minus1 = reader
                        .readUE("SH: num_ref_idx_l0_active_minus1");
                if (sh.slice_type == SliceType.B) {
                    sh.num_ref_idx_l1_active_minus1 = reader
                            .readUE("SH: num_ref_idx_l1_active_minus1");
                }
            }
        }
        readRefPicListReordering(sh, reader);
        if ((sps.weighted_pred_flag && (sh.slice_type == SliceType.P || sh.slice_type == SliceType.SP))
                || (sps.weighted_bipred_idc == 1 && sh.slice_type == SliceType.B))
            readPredWeightTable(sps, pps, sh, reader);
        if (nalUnit.nal_ref_idc != 0)
            readDecoderPicMarking(nalUnit, sh, reader);
        if (sps.entropy_coding_mode_flag && sh.slice_type != SliceType.I
                && sh.slice_type != SliceType.SI) {
            sh.cabac_init_idc = reader.readUE("SH: cabac_init_idc");
        }
        sh.slice_qp_delta = reader.readSE("SH: slice_qp_delta");
        if (sh.slice_type == SliceType.SP || sh.slice_type == SliceType.SI) {
            if (sh.slice_type == SliceType.SP) {
                sh.sp_for_switch_flag = reader
                        .readBool("SH: sp_for_switch_flag");
            }
            sh.slice_qs_delta = reader.readSE("SH: slice_qs_delta");
        }
        if (pps.deblocking_filter_control_present_flag) {
            sh.disable_deblocking_filter_idc = reader
                    .readUE("SH: disable_deblocking_filter_idc");
            if (sh.disable_deblocking_filter_idc != 1) {
                sh.slice_alpha_c0_offset_div2 = reader
                        .readSE("SH: slice_alpha_c0_offset_div2");
                sh.slice_beta_offset_div2 = reader
                        .readSE("SH: slice_beta_offset_div2");
            }
        }
        if (pps.num_slice_groups_minus1 > 0 && pps.slice_group_map_type >= 3
                && pps.slice_group_map_type <= 5) {
            int len = (sps.pic_height_in_map_units_minus1 + 1)
                    * (sps.pic_width_in_mbs_minus1 + 1)
                    / (pps.slice_group_change_rate_minus1 + 1);
            if (((sps.pic_height_in_map_units_minus1 + 1) * (sps.pic_width_in_mbs_minus1 + 1))
                    % (pps.slice_group_change_rate_minus1 + 1) > 0)
                len += 1;

            len = CeilLog2(len + 1);
            sh.slice_group_change_cycle = reader.readU(len,
                    "SH: slice_group_change_cycle");
        }

        return sh;
    }

    private static int CeilLog2(int uiVal) {
        int uiTmp = uiVal - 1;
        int uiRet = 0;

        while (uiTmp != 0) {
            uiTmp >>= 1;
            uiRet++;
        }
        return uiRet;
    }

    private static void readDecoderPicMarking(NALUnit nalUnit, SliceHeader sh,
                                              CAVLCReader reader) throws IOException {
        if (nalUnit.type == NALUnitType.IDR_SLICE) {
            boolean no_output_of_prior_pics_flag = reader
                    .readBool("SH: no_output_of_prior_pics_flag");
            boolean long_term_reference_flag = reader
                    .readBool("SH: long_term_reference_flag");
            sh.refPicMarkingIDR = new RefPicMarkingIDR(
                    no_output_of_prior_pics_flag, long_term_reference_flag);
        } else {
            boolean adaptive_ref_pic_marking_mode_flag = reader
                    .readBool("SH: adaptive_ref_pic_marking_mode_flag");
            if (adaptive_ref_pic_marking_mode_flag) {
                ArrayList<RefPicMarking.Instruction> mmops = new ArrayList<RefPicMarking.Instruction>();
                int memory_management_control_operation;
                do {
                    memory_management_control_operation = reader
                            .readUE("SH: memory_management_control_operation");

                    RefPicMarking.Instruction instr = null;

                    switch (memory_management_control_operation) {
                        case 1:
                            instr = new RefPicMarking.Instruction(
                                    RefPicMarking.InstrType.REMOVE_SHORT,
                                    reader
                                            .readUE("SH: difference_of_pic_nums_minus1") + 1,
                                    0);
                            break;
                        case 2:
                            instr = new RefPicMarking.Instruction(
                                    RefPicMarking.InstrType.REMOVE_LONG, reader
                                    .readUE("SH: long_term_pic_num"), 0);
                            break;
                        case 3:
                            instr = new RefPicMarking.Instruction(
                                    RefPicMarking.InstrType.CONVERT_INTO_LONG,
                                    reader
                                            .readUE("SH: difference_of_pic_nums_minus1") + 1,
                                    reader.readUE("SH: long_term_frame_idx"));
                            break;
                        case 4:
                            instr = new RefPicMarking.Instruction(
                                    RefPicMarking.InstrType.TRUNK_LONG,
                                    reader
                                            .readUE("SH: max_long_term_frame_idx_plus1"),
                                    0);
                            break;
                        case 5:
                            instr = new RefPicMarking.Instruction(RefPicMarking.InstrType.CLEAR,
                                    0, 0);
                            break;
                        case 6:
                            instr = new RefPicMarking.Instruction(
                                    RefPicMarking.InstrType.MARK_LONG, reader
                                    .readUE("SH: long_term_frame_idx"), 0);
                            break;
                    }
                    if (instr != null)
                        mmops.add(instr);
                } while (memory_management_control_operation != 0);
                sh.refPicMarkingNonIDR = new RefPicMarking(mmops
                        .toArray(new RefPicMarking.Instruction[]{}));
            }
        }
    }

    private static void readPredWeightTable(SeqParameterSet sps,
                                            PictureParameterSet pps, SliceHeader sh, CAVLCReader reader)
            throws IOException {
        sh.pred_weight_table.luma_log2_weight_denom = reader
                .readUE("SH: luma_log2_weight_denom");
        if (sps.chroma_format_idc != ChromaFormat.MONOCHROME) {
            sh.pred_weight_table.chroma_log2_weight_denom = reader
                    .readUE("SH: chroma_log2_weight_denom");
        }
        sh.pred_weight_table.luma_offset_weight_l0 = new PredictionWeightTable.OffsetWeight[pps.num_ref_idx_l0_active_minus1];
        sh.pred_weight_table.chroma_offset_weight_l0 = new PredictionWeightTable.OffsetWeight[pps.num_ref_idx_l0_active_minus1][];
        for (int i = 0; i <= pps.num_ref_idx_l0_active_minus1; i++) {
            boolean luma_weight_l0_flag = reader
                    .readBool("SH: luma_weight_l0_flag");
            if (luma_weight_l0_flag) {
                PredictionWeightTable.OffsetWeight ow = new PredictionWeightTable.OffsetWeight();
                ow.weight = reader.readSE("SH: weight");
                ow.offset = reader.readSE("SH: offset");

                sh.pred_weight_table.luma_offset_weight_l0[i] = ow;
            }
            if (sps.chroma_format_idc != ChromaFormat.MONOCHROME) {
                boolean chroma_weight_l0_flag = reader
                        .readBool("SH: chroma_weight_l0_flag");
                if (chroma_weight_l0_flag)
                    for (int j = 0; j < 2; j++) {
                        PredictionWeightTable.OffsetWeight ow = new PredictionWeightTable.OffsetWeight();
                        ow.weight = reader.readSE("SH: weight");
                        ow.offset = reader.readSE("SH: offset");
                        sh.pred_weight_table.chroma_offset_weight_l0[i][j] = ow;
                    }
            }
        }
        if (sh.slice_type == SliceType.B) {
            sh.pred_weight_table.luma_offset_weight_l1 = new PredictionWeightTable.OffsetWeight[pps.num_ref_idx_l1_active_minus1];
            sh.pred_weight_table.chroma_offset_weight_l1 = new PredictionWeightTable.OffsetWeight[pps.num_ref_idx_l1_active_minus1][];
            for (int i = 0; i <= pps.num_ref_idx_l1_active_minus1; i++) {
                boolean luma_weight_l1_flag = reader
                        .readBool("SH: luma_weight_l1_flag");
                if (luma_weight_l1_flag) {
                    PredictionWeightTable.OffsetWeight ow = new PredictionWeightTable.OffsetWeight();
                    ow.weight = reader.readSE("SH: weight");
                    ow.offset = reader.readSE("SH: offset");
                    sh.pred_weight_table.luma_offset_weight_l1[i] = ow;
                }
                if (sps.chroma_format_idc != ChromaFormat.MONOCHROME) {
                    boolean chroma_weight_l1_flag = reader
                            .readBool("SH: chroma_weight_l1_flag");
                    if (chroma_weight_l1_flag) {
                        for (int j = 0; j < 2; j++) {
                            PredictionWeightTable.OffsetWeight ow = new PredictionWeightTable.OffsetWeight();
                            ow.weight = reader.readSE("SH: weight");
                            ow.offset = reader.readSE("SH: offset");
                            sh.pred_weight_table.chroma_offset_weight_l1[i][j] = ow;
                        }
                    }
                }
            }
        }
    }

    private static void readRefPicListReordering(SliceHeader sh,
                                                 CAVLCReader reader) throws IOException {
        if (sh.slice_type != SliceType.I && sh.slice_type != SliceType.SI) {
            boolean ref_pic_list_reordering_flag_l0 = reader
                    .readBool("SH: ref_pic_list_reordering_flag_l0");
            if (ref_pic_list_reordering_flag_l0) {
                sh.refPicReordering = readReorderingEntries(reader);
            }
        }
        if (sh.slice_type == SliceType.B) {
            boolean ref_pic_list_reordering_flag_l1 = reader
                    .readBool("SH: ref_pic_list_reordering_flag_l1");
            if (ref_pic_list_reordering_flag_l1) {
                readReorderingEntries(reader);
            }
        }
    }

    private static RefPicReordering readReorderingEntries(CAVLCReader reader)
            throws IOException {
        ArrayList<RefPicReordering.ReorderOp> reordering = new ArrayList<RefPicReordering.ReorderOp>();
        int reordering_of_pic_nums_idc;
        do {
            reordering_of_pic_nums_idc = reader
                    .readUE("SH: reordering_of_pic_nums_idc");
            switch (reordering_of_pic_nums_idc) {
                case 0:
                    reordering.add(new RefPicReordering.ReorderOp(RefPicReordering.InstrType.BACKWARD, reader
                            .readUE("SH: abs_diff_pic_num_minus1") + 1));
                    break;
                case 1:
                    reordering.add(new RefPicReordering.ReorderOp(RefPicReordering.InstrType.FORWARD, reader
                            .readUE("SH: abs_diff_pic_num_minus1") + 1));
                    break;
                case 2:
                    reordering.add(new RefPicReordering.ReorderOp(RefPicReordering.InstrType.LONG_TERM, reader
                            .readUE("SH: long_term_pic_num")));
                    break;
            }
        } while (reordering_of_pic_nums_idc != 3);
        return new RefPicReordering(reordering.toArray(new RefPicReordering.ReorderOp[]{}));
    }
}
