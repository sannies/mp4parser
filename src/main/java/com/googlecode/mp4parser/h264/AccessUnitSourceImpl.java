package com.googlecode.mp4parser.h264;

import com.coremedia.iso.IsoBufferWrapper;
import com.googlecode.mp4parser.h264.model.NALUnit;
import com.googlecode.mp4parser.h264.model.NALUnitType;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;
import com.googlecode.mp4parser.h264.model.SliceHeader;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.h264.read.SliceHeaderReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 */
public class AccessUnitSourceImpl implements AccessUnitSource, StreamParams {
    private Map<Integer, SeqParameterSet> seqParameterSetMap = new HashMap<Integer, SeqParameterSet>();
    private Map<Integer, PictureParameterSet> pictureParameterSetMap = new HashMap<Integer, PictureParameterSet>();

    NALUnitReader nalUnitReader;
    IsoBufferWrapper currentNal;
    SliceHeaderReader sliceHeaderReader = new SliceHeaderReader(this);

    public AccessUnitSourceImpl(NALUnitReader nalUnitReader) throws IOException {
        this.nalUnitReader = nalUnitReader;
        currentNal = nalUnitReader.nextNALUnit();
    }

    public AccessUnit nextAccessUnit() throws IOException {
        if (currentNal == null) {
            return null;
        }
        List<IsoBufferWrapper> au = new LinkedList<IsoBufferWrapper>();
        NALUnit oldNu = null;
        SliceHeader oldSh = null;
        boolean nalEnd = false;
        do {
            currentNal.position(0);
            NALUnit nu = NALUnit.read(currentNal);
            if (nu.type == NALUnitType.SPS) {
                SeqParameterSet sps = SeqParameterSet.read(currentNal);
                seqParameterSetMap.put(sps.seq_parameter_set_id, sps);
            } else if (nu.type == NALUnitType.PPS) {
                PictureParameterSet pps = PictureParameterSet.read(currentNal);
                pictureParameterSetMap.put(pps.pic_parameter_set_id, pps);

            } else if (nal_type_ok_for_sample.contains(nu.type)) {
                if (nu.type == NALUnitType.IDR_SLICE
                        || nu.type == NALUnitType.NON_IDR_SLICE) {
                    SliceHeader sh = sliceHeaderReader.read(nu, new CAVLCReader(currentNal));
                    if (oldNu != null && oldSh != null
                            && !sameAccessUnit(oldNu, nu, oldSh, sh)) {
                        nalEnd = true;
                    } else {
                        oldNu = nu;
                        oldSh = sh;
                        currentNal.position(0);
                        au.add(currentNal);
                    }
                } else if (oldNu != null && nu.type == NALUnitType.SEI) {
                    nalEnd = true;
                } else {
                    currentNal.position(0);
                    au.add(currentNal);
                }
            }

        }
        while (!nalEnd && (currentNal = nalUnitReader.nextNALUnit()) != null);

        return new AccessUnitImpl(au);
    }

    public SeqParameterSet getSPS(int id) {
        return seqParameterSetMap.get(id);
    }

    public PictureParameterSet getPPS(int id) {
        return pictureParameterSetMap.get(id);
    }


    private class AccessUnitImpl implements AccessUnit {
        LinkedList<IsoBufferWrapper> nals;

        private AccessUnitImpl(List<IsoBufferWrapper> nals) {
            this.nals = new LinkedList<IsoBufferWrapper>(nals);
        }

        public IsoBufferWrapper nextNALUnit() throws IOException {
            try {
                return nals.pop();
            } catch (NoSuchElementException e) {
                return null;
            }
        }
    }

    static Set<NALUnitType> nal_type_ok_for_sample = new HashSet<NALUnitType>() {
        {
            this.add(NALUnitType.NON_IDR_SLICE);
            this.add(NALUnitType.SLICE_PART_A);
            this.add(NALUnitType.SLICE_PART_B);
            this.add(NALUnitType.SLICE_PART_C);
            this.add(NALUnitType.IDR_SLICE);
            this.add(NALUnitType.SEI);
            this.add(NALUnitType.AUX_SLICE);
        }
    };

    private boolean sameAccessUnit(NALUnit nu1, NALUnit nu2, SliceHeader sh1,
                                   SliceHeader sh2) {
        if (sh1.pic_parameter_set_id != sh2.pic_parameter_set_id)
            return false;

        if (sh1.frame_num != sh2.frame_num)
            return false;

        PictureParameterSet pps = pictureParameterSetMap.get(sh1.pic_parameter_set_id);
        SeqParameterSet sps = seqParameterSetMap.get(pps.seq_parameter_set_id);

        if ((sps.pic_order_cnt_type == 0 && sh1.pic_order_cnt_lsb != sh2.pic_order_cnt_lsb))
            return false;

        if ((sps.pic_order_cnt_type == 1 && (sh1.delta_pic_order_cnt[0] != sh2.delta_pic_order_cnt[0] || sh1.delta_pic_order_cnt[1] != sh2.delta_pic_order_cnt[1])))
            return false;

        if (((nu1.nal_ref_idc == 0 || nu2.nal_ref_idc == 0) && nu1.nal_ref_idc != nu2.nal_ref_idc))
            return false;

        if (((nu1.type == NALUnitType.IDR_SLICE) != (nu2.type == NALUnitType.IDR_SLICE)))
            return false;

        if (sh1.idr_pic_id != sh2.idr_pic_id)
            return false;

        return true;
    }


}
