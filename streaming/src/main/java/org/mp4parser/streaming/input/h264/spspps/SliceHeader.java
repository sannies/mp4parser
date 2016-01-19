package org.mp4parser.streaming.input.h264.spspps;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class SliceHeader {

    public int first_mb_in_slice;
    public SliceType slice_type;
    public int pic_parameter_set_id;
    public int colour_plane_id;
    public int frame_num;
    public boolean field_pic_flag = false;
    public boolean bottom_field_flag = false;
    public int idr_pic_id = -1;
    public int pic_order_cnt_lsb;
    public int delta_pic_order_cnt_bottom;
    public int delta_pic_order_cnt_0;
    public int delta_pic_order_cnt_1;
    public PictureParameterSet pps;
    public SeqParameterSet sps;

    public SliceHeader(ByteBuffer in, Map<Integer, SeqParameterSet> spss, Map<Integer, PictureParameterSet> ppss, boolean IdrPicFlag) {
        try {
            in.position(1);
            ByteBufferBitreader reader = new ByteBufferBitreader(in);
            first_mb_in_slice = reader.readUE();
            int sliceTypeInt = reader.readUE();
            switch (sliceTypeInt) {
                case 0:
                case 5:
                    slice_type = SliceType.P;
                    break;

                case 1:
                case 6:
                    slice_type = SliceType.B;
                    break;

                case 2:
                case 7:
                    slice_type = SliceType.I;
                    break;

                case 3:
                case 8:
                    slice_type = SliceType.SP;
                    break;

                case 4:
                case 9:
                    slice_type = SliceType.SI;
                    break;

            }

            pic_parameter_set_id = reader.readUE();
            pps = ppss.get(pic_parameter_set_id);
            if (pps == null) {
                String ids = "";
                for (Integer integer : ppss.keySet()) {
                    ids += integer + ", ";
                }
                throw new RuntimeException("PPS with ids " + ids + " available but not " + pic_parameter_set_id);
            }
            sps = spss.get(pps.seq_parameter_set_id);
            if (sps == null) {
                String ids = "";
                for (Integer integer : spss.keySet()) {
                    ids += integer + ", ";
                }
                throw new RuntimeException("SPS with ids " + ids + " available but not " + pps.seq_parameter_set_id);
            }
            if (sps.residual_color_transform_flag) {
                colour_plane_id = (int) reader.readNBit(2);
            }
            frame_num = (int) reader.readNBit(sps.log2_max_frame_num_minus4 + 4);
            if (!sps.frame_mbs_only_flag) {
                field_pic_flag = reader.readBool();
                if (field_pic_flag) {
                    bottom_field_flag = reader.readBool();
                }
            }
            if (IdrPicFlag) {
                idr_pic_id = reader.readUE();
            }
            if (sps.pic_order_cnt_type == 0) {
                pic_order_cnt_lsb = (int) reader.readNBit(sps.log2_max_pic_order_cnt_lsb_minus4 + 4);
                if (pps.bottom_field_pic_order_in_frame_present_flag && !field_pic_flag) {
                    delta_pic_order_cnt_bottom = reader.readSE();
                }
            }

            if (sps.pic_order_cnt_type == 1 && !sps.delta_pic_order_always_zero_flag) {

                delta_pic_order_cnt_0 = reader.readSE();
                if (pps.bottom_field_pic_order_in_frame_present_flag && !field_pic_flag) {
                    delta_pic_order_cnt_1 = reader.readSE();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String toString() {
        return "SliceHeader{" +
                "first_mb_in_slice=" + first_mb_in_slice +
                ", slice_type=" + slice_type +
                ", pic_parameter_set_id=" + pic_parameter_set_id +
                ", colour_plane_id=" + colour_plane_id +
                ", frame_num=" + frame_num +
                ", field_pic_flag=" + field_pic_flag +
                ", bottom_field_flag=" + bottom_field_flag +
                ", idr_pic_id=" + idr_pic_id +
                ", pic_order_cnt_lsb=" + pic_order_cnt_lsb +
                ", delta_pic_order_cnt_bottom=" + delta_pic_order_cnt_bottom +
                '}';
    }

    public enum SliceType {
        P, B, I, SP, SI
    }
}
