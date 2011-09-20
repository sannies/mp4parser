package com.googlecode.mp4parser.h264;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.googlecode.mp4parser.h264.model.NALUnit;
import com.googlecode.mp4parser.h264.model.NALUnitType;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;
import com.googlecode.mp4parser.h264.model.SliceHeader;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.h264.read.SliceHeaderReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extracts an access unit coded as a sequence of NAL unit in Annex B format
 *
 * @author Stanislav Vitvitskiy
 */
public class AnnexB2SampleDemuxer implements StreamParams {
    private Map<Integer, SeqParameterSet> spsSet = new HashMap<Integer, SeqParameterSet>();
    private Map<Integer, PictureParameterSet> ppsSet = new HashMap<Integer, PictureParameterSet>();

    private SliceHeaderReader sliceHeaderReader;
    private NALUnitSource nuSource;
    private IsoBufferWrapper currentUnit;

    private class AnnexBAccessUnit implements AccessUnit {
        private NALUnit oldNu;
        private SliceHeader oldSh;

        public IsoBufferWrapper nextNALUnit() throws IOException {
            IsoBufferWrapper is;
            while ((is = nextNALUnitSub()) == NOT_VALID_FOR_SAMPLE) {
                System.err.println("NAL - Not in Sample");
            }

            return is;
        }

        private IsoBufferWrapper nextNALUnitSub() throws IOException {
            if (currentUnit == null)
                return null;

            currentUnit.position(0);
            NALUnit nu = NALUnit.read(currentUnit);

            if (!nal_type_ok_for_sample.contains(nu.type)) {
                processNonPictureUnit(nu, currentUnit);
                currentUnit = readNextUnit();
                return NOT_VALID_FOR_SAMPLE;
            }
            if (nu.type == NALUnitType.IDR_SLICE
                    || nu.type == NALUnitType.NON_IDR_SLICE) {

                SliceHeader sh = sliceHeaderReader.read(nu, new CAVLCReader(currentUnit));
                if (oldNu != null && oldSh != null
                        && !sameAccessUnit(oldNu, nu, oldSh, sh)) {
                    return null;
                }

                oldNu = nu;
                oldSh = sh;

            } else if (nu.type == NALUnitType.SEI) {
                if (oldNu != null) {
                    return null;
                }
            } else if (nu.type == NALUnitType.ACC_UNIT_DELIM) {
                return null;
            }

            currentUnit = readNextUnit();

            return currentUnit;
        }
    }

    public AnnexB2SampleDemuxer(IsoBufferWrapper src)
            throws IOException {

        sliceHeaderReader = new SliceHeaderReader(this);
        this.nuSource = new NALUnitReader(src);

        currentUnit = readNextUnit();
    }

    private final IsoBufferWrapperImpl NOT_VALID_FOR_SAMPLE = new IsoBufferWrapperImpl(new byte[]{}) {
    };

    private IsoBufferWrapper readNextUnit() throws IOException {
        return nuSource.nextNALUnit();
    }

    private void processNonPictureUnit(NALUnit nu, IsoBufferWrapper is)
            throws IOException {
        if (nu.type == NALUnitType.SPS) {
            SeqParameterSet sps = SeqParameterSet.read(is);
            spsSet.put(sps.seq_parameter_set_id, sps);
        } else if (nu.type == NALUnitType.PPS) {
            PictureParameterSet pps = PictureParameterSet.read(is);
            ppsSet.put(pps.pic_parameter_set_id, pps);
        }

    }

    private boolean sameAccessUnit(NALUnit nu1, NALUnit nu2, SliceHeader sh1,
                                   SliceHeader sh2) {
        if (sh1.pic_parameter_set_id != sh2.pic_parameter_set_id)
            return false;

        if (sh1.frame_num != sh2.frame_num)
            return false;

        PictureParameterSet pps = ppsSet.get(sh1.pic_parameter_set_id);
        SeqParameterSet sps = spsSet.get(pps.seq_parameter_set_id);

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

    public AccessUnit nextAcceessUnit() {
        if (currentUnit != null)
            return new AnnexBAccessUnit();

        return null;
    }

    public interface NALUnitSource {
        IsoBufferWrapper nextNALUnit() throws IOException;
    }

    public PictureParameterSet getPPS(int id) {
        return ppsSet.get(id);
    }

    public SeqParameterSet getSPS(int id) {
        return spsSet.get(id);
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        assert input != null && output != null;

        byte[] buffer = new byte[4096];
        int count = input.read(buffer);
        while (count > 0) {
            output.write(buffer, 0, count);
            count = input.read(buffer);
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

}