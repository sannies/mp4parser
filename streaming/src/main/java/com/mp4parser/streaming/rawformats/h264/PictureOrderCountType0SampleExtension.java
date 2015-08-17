package com.mp4parser.streaming.rawformats.h264;

import com.mp4parser.muxer.tracks.h264.SliceHeader;
import com.mp4parser.streaming.SampleExtension;
import com.mp4parser.tools.Mp4Arrays;

/**
 * Created by sannies on 17.08.2015.
 */
class PictureOrderCountType0SampleExtension implements SampleExtension {
    int picOrderCntMsb;
    int picOrderCountLsb;

    public PictureOrderCountType0SampleExtension(SliceHeader currentSlice, PictureOrderCountType0SampleExtension previous) {
        int prevPicOrderCntLsb = 0;
        int prevPicOrderCntMsb = 0;
        if (previous != null) {
            prevPicOrderCntLsb = previous.picOrderCountLsb;
            prevPicOrderCntMsb = previous.picOrderCntMsb;
        }

        int max_pic_order_count_lsb = (1 << (currentSlice.sps.log2_max_pic_order_cnt_lsb_minus4 + 4));
        // System.out.print(" pic_order_cnt_lsb " + pic_order_cnt_lsb + " " + max_pic_order_count);
        picOrderCountLsb = currentSlice.pic_order_cnt_lsb;
        picOrderCntMsb = 0;
        if ((picOrderCountLsb < prevPicOrderCntLsb) &&
                ((prevPicOrderCntLsb - picOrderCountLsb) >= (max_pic_order_count_lsb / 2))) {
            picOrderCntMsb = prevPicOrderCntMsb + max_pic_order_count_lsb;
        } else if ((picOrderCountLsb > prevPicOrderCntLsb) &&
                ((picOrderCountLsb - prevPicOrderCntLsb) > (max_pic_order_count_lsb / 2))) {
            picOrderCntMsb = prevPicOrderCntMsb - max_pic_order_count_lsb;
        } else {
            picOrderCntMsb = prevPicOrderCntMsb;
        }
    }

    public int getPoc() {
        return picOrderCntMsb + picOrderCountLsb;
    }

    @Override
    public String toString() {
        return "picOrderCntMsb=" + picOrderCntMsb + ", picOrderCountLsb=" + picOrderCountLsb;
    }
}
