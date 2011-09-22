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

/**
 * Slice header H264 bitstream entity
 * <p/>
 * capable to serialize / deserialize with CAVLC bitstream
 *
 * @author Stanislav Vitvitskiy
 */
public class SliceHeader {

    public SeqParameterSet sps;
    public PictureParameterSet pps;

    public RefPicMarking refPicMarkingNonIDR;
    public RefPicMarkingIDR refPicMarkingIDR;
    public RefPicReordering refPicReordering;

    public PredictionWeightTable pred_weight_table;
    public int first_mb_in_slice;

    public boolean field_pic_flag;

    public SliceType slice_type;
    public boolean slice_type_restr;

    public int pic_parameter_set_id;

    public int frame_num;

    public boolean bottom_field_flag;

    public int idr_pic_id;

    public int pic_order_cnt_lsb;

    public int delta_pic_order_cnt_bottom;

    public int[] delta_pic_order_cnt;

    public int redundant_pic_cnt;

    public boolean direct_spatial_mv_pred_flag;

    public boolean num_ref_idx_active_override_flag;

    public int num_ref_idx_l0_active_minus1;

    public int num_ref_idx_l1_active_minus1;

    public int cabac_init_idc;

    public int slice_qp_delta;

    public boolean sp_for_switch_flag;

    public int slice_qs_delta;

    public int disable_deblocking_filter_idc;

    public int slice_alpha_c0_offset_div2;

    public int slice_beta_offset_div2;

    public int slice_group_change_cycle;

    @Override
    public String toString() {
        return "SliceHeader{" +
                "\n       sps=" + sps.seq_parameter_set_id +
                ",\n       pps=" + pps.pic_parameter_set_id +
                (refPicMarkingNonIDR != null ? ",\n       refPicMarkingNonIDR=" + refPicMarkingNonIDR : "") +
                (refPicMarkingIDR != null ? ",\n       refPicMarkingIDR=" + refPicMarkingIDR : "") +
                (refPicReordering != null ? ",\n       refPicReordering=" + refPicReordering : "") +
                (pred_weight_table != null ? ",\n       pred_weight_table=" + pred_weight_table : "") +
                ",\n       first_mb_in_slice=" + first_mb_in_slice +
                ",\n       field_pic_flag=" + field_pic_flag +
                ",\n       slice_type=" + slice_type +
                ",\n       slice_type_restr=" + slice_type_restr +
                ",\n       pic_parameter_set_id=" + pic_parameter_set_id +
                ",\n       frame_num=" + frame_num +
                ",\n       bottom_field_flag=" + bottom_field_flag +
                ",\n       idr_pic_id=" + idr_pic_id +
                ",\n       pic_order_cnt_lsb=" + pic_order_cnt_lsb +
                ",\n       delta_pic_order_cnt_bottom=" + delta_pic_order_cnt_bottom +
                ",\n       delta_pic_order_cnt=" + intArrToString(delta_pic_order_cnt) +
                ",\n       redundant_pic_cnt=" + redundant_pic_cnt +
                ",\n       direct_spatial_mv_pred_flag=" + direct_spatial_mv_pred_flag +
                ",\n       num_ref_idx_active_override_flag=" + num_ref_idx_active_override_flag +
                ",\n       num_ref_idx_l0_active_minus1=" + num_ref_idx_l0_active_minus1 +
                ",\n       num_ref_idx_l1_active_minus1=" + num_ref_idx_l1_active_minus1 +
                ",\n       cabac_init_idc=" + cabac_init_idc +
                ",\n       slice_qp_delta=" + slice_qp_delta +
                ",\n       sp_for_switch_flag=" + sp_for_switch_flag +
                ",\n       slice_qs_delta=" + slice_qs_delta +
                ",\n       disable_deblocking_filter_idc=" + disable_deblocking_filter_idc +
                ",\n       slice_alpha_c0_offset_div2=" + slice_alpha_c0_offset_div2 +
                ",\n       slice_beta_offset_div2=" + slice_beta_offset_div2 +
                ",\n       slice_group_change_cycle=" + slice_group_change_cycle +
                '}';
    }

    private String intArrToString(int[] delta_pic_order_cnt) {
        String rc = "";
        for (int i : delta_pic_order_cnt) {
            rc += i + ", ";
        }
        return rc;
    }
}
