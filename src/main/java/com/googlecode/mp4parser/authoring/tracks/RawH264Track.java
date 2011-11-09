package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.MultiplexIsoBufferWrapperImpl;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.h264.*;
import com.googlecode.mp4parser.h264.model.*;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.h264.read.SliceHeaderReader;
import com.googlecode.mp4parser.util.Path;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class RawH264Track extends AbstractTrack implements StreamParams {
    private static int a;
    List<IsoBufferWrapper> samples = new ArrayList<IsoBufferWrapper>();
    StreamParams streamParams;

    double fps = 30;

    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(new File("/home/sannies/vw.mp4")));
        Path p = new Path(isoFile);
        isoFile.parse();
        CompositionTimeToSample ctts = (CompositionTimeToSample) p.getPath("/moov/trak[1]/mdia/minf/stbl/ctts");
        int[] cts = CompositionTimeToSample.blowupCompositionTimes(ctts.getEntries());
        RawH264Track track = new RawH264Track(new IsoBufferWrapperImpl(new File("/home/sannies/vw_track2.h264")));
        //RawH264Track track = new RawH264Track(new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-samurai_h640w_track1.h264")));
    }

    public RawH264Track(IsoBufferWrapperImpl rawH264) throws IOException {
        InnerAccessUnit current = new InnerAccessUnit();
        InnerAccessUnit previous = new InnerAccessUnit();


        NALUnitReader nalUnitReader = new AnnexBNALUnitReader(rawH264);
        AccessUnitSourceImpl accessUnitSource = new AccessUnitSourceImpl(nalUnitReader);
        streamParams = accessUnitSource;
        SliceHeaderReader sliceHeaderReader = new SliceHeaderReader(accessUnitSource);
        AccessUnit au;
        long frameNumInGop = 0;
        while ((au = accessUnitSource.nextAccessUnit()) != null) {
            //System.err.println("Start AU");
            List<IsoBufferWrapper> nals = new LinkedList<IsoBufferWrapper>();
            IsoBufferWrapper nalUnitBuffer;
            while ((nalUnitBuffer = au.nextNALUnit()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new IsoOutputStream(baos).writeUInt32(nalUnitBuffer.size());
                nals.add(new IsoBufferWrapperImpl(baos.toByteArray()));
                nals.add(nalUnitBuffer);
                NALUnit nalUnit = NALUnit.read(nalUnitBuffer);
                if (nalUnit.type == NALUnitType.IDR_SLICE ||
                        nalUnit.type == NALUnitType.NON_IDR_SLICE ||
                        nalUnit.type == NALUnitType.AUX_SLICE ||
                        nalUnit.type == NALUnitType.SLICE_PART_A ||
                        nalUnit.type == NALUnitType.SLICE_PART_B ||
                        nalUnit.type == NALUnitType.SLICE_PART_C) {
                    current.sliceHeaders.add(sliceHeaderReader.read(nalUnit, new CAVLCReader(nalUnitBuffer)));
                }

                if (++a % 1000 == 0) {
                    System.err.println(a);
                }
            }
            samples.add(new MultiplexIsoBufferWrapperImpl(nals));
            decodedPoc(current, previous);
            if (current.poc == 0) {
                frameNumInGop = 0;
            }
            System.err.println("cts: " +  ( current.poc - frameNumInGop * 2 ));
            previous = current;
            frameNumInGop++;
            current = new InnerAccessUnit();
        }
    }

    public List<IsoBufferWrapper> getSamples() {
        return samples;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        SampleDescriptionBox sampleDescriptionBox = new SampleDescriptionBox();

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<TimeToSampleBox.Entry> getDecodingTimeEntries() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        /*
        MP4AV_calculate_dts_from_pts  in mp4creator's mpeg4.cpp
         */
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long[] getSyncSamples() {
        return new long[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TrackMetaData getTrackMetaData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Type getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SeqParameterSet getSPS(int id) {
        return streamParams.getSPS(id);
    }

    public PictureParameterSet getPPS(int id) {
        return streamParams.getPPS(id);
    }


    static class InnerAccessUnit {
        int decodingTime;
        int picOrderCntMsb;
        int poc;
        SEI sei;
        List<SliceHeader> sliceHeaders = new LinkedList<SliceHeader>();
    }


    static InnerAccessUnit decodePocType0(InnerAccessUnit current, InnerAccessUnit prev) {
        int TopFieldOrderCnt = Integer.MAX_VALUE;
        int BottomFieldOrderCnt = Integer.MAX_VALUE;


        int prevPicOrderCntMsb;
        int prevPicOrderCntLsb;

        if (current.sliceHeaders.get(0).slice_type == SliceType.I || current.sliceHeaders.get(0).slice_type == SliceType.SI) {
            prevPicOrderCntMsb = 0;
            prevPicOrderCntLsb = 0;
        } else {
            // if memory management dunno
            if (current.sei != null) {
                for (SEI.SEIMessage message : current.sei.messages) {
                    if (message.payloadType == SEI.PIC_TIMING) {
                        throw new RuntimeException("That needs to be implemented. 7687526897568234.");
                    }
                }
            }
            prevPicOrderCntMsb = prev.picOrderCntMsb;
            prevPicOrderCntLsb = prev.sliceHeaders.get(0).pic_order_cnt_lsb;
        }
        int MaxPicOrderCntLsb = 1 << (current.sliceHeaders.get(0).sps.log2_max_pic_order_cnt_lsb_minus4 + 4);
        int picOrderCntMsb;

        if ((current.sliceHeaders.get(0).pic_order_cnt_lsb < prevPicOrderCntLsb) &&
                ((prevPicOrderCntLsb - current.sliceHeaders.get(0).pic_order_cnt_lsb) >= (MaxPicOrderCntLsb / 2))) {
            picOrderCntMsb = prevPicOrderCntMsb + MaxPicOrderCntLsb;
        } else if ((current.sliceHeaders.get(0).pic_order_cnt_lsb > prevPicOrderCntLsb) &&
                ((current.sliceHeaders.get(0).pic_order_cnt_lsb - prevPicOrderCntLsb) > (MaxPicOrderCntLsb / 2))) {
            picOrderCntMsb = prevPicOrderCntMsb - MaxPicOrderCntLsb;
        } else {
            picOrderCntMsb = prevPicOrderCntMsb;
        }

        if (!current.sliceHeaders.get(0).bottom_field_flag) {
            TopFieldOrderCnt = picOrderCntMsb + current.sliceHeaders.get(0).pic_order_cnt_lsb;
        }

        if (current.sliceHeaders.get(0).bottom_field_flag) {
            if (!current.sliceHeaders.get(0).field_pic_flag) {
                BottomFieldOrderCnt = TopFieldOrderCnt + current.sliceHeaders.get(0).delta_pic_order_cnt_bottom;
            } else {
                BottomFieldOrderCnt = picOrderCntMsb + current.sliceHeaders.get(0).pic_order_cnt_lsb;
            }
        }


        if (current.sliceHeaders.get(0).sps.frame_mbs_only_flag || !current.sliceHeaders.get(0).field_pic_flag)
            current.poc = Math.min(TopFieldOrderCnt, BottomFieldOrderCnt);
        else if (current.sliceHeaders.get(0).bottom_field_flag)
            current.poc = BottomFieldOrderCnt;
        else
            current.poc = TopFieldOrderCnt;

    //    System.err.println("poc     : " + current.poc);
    //    System.err.println("poc-diff: " + (current.poc - prev.poc));
        return current;
    }

    static InnerAccessUnit decodePocType1(InnerAccessUnit current, InnerAccessUnit prev) {
        System.err.println("decodePocType1");
        throw new UnsupportedOperationException("Please implement decodePocType1");
    }

    static InnerAccessUnit decodePocType2(InnerAccessUnit current, InnerAccessUnit prev) {
        System.err.println("decodePocType2");
        throw new UnsupportedOperationException("Please implement decodePocType2");
    }

    static InnerAccessUnit insertDecodingTime(InnerAccessUnit current, InnerAccessUnit previous) {
        int DeltaTfiDivisorIdx;
        if (!current.sliceHeaders.get(0).sps.vuiParams.pic_struct_present_flag) {
            DeltaTfiDivisorIdx = 1 + (1 - (!current.sliceHeaders.get(0).field_pic_flag ? 0 : 1));
        } else {
            throw new UnsupportedOperationException("Hmm I cannot deal with picTimingSei");
            // get details here
            // D.1.2 Picture timing SEI message syntax


            /*SEI.SEIMessage picTimingSei = null;
           for (SEI.SEIMessage message : current.sei.messages) {
               if (message.payloadType == 1) {
                   picTimingSei = message;
               }
           }
           if (picTimingSei != null) {

               if (!avc.sei.pic_timing.pic_struct)
                   DeltaTfiDivisorIdx = 2;
               else if (avc.sei.pic_timing.pic_struct == 8)
                  DeltaTfiDivisorIdx = 6;
                else
                DeltaTfiDivisorIdx = (avc.sei.pic_timing.pic_struct + 1) / 2;
           } */
        }

        current.decodingTime = previous.decodingTime + 2 * current.sliceHeaders.get(0).sps.vuiParams.num_units_in_tick * DeltaTfiDivisorIdx;
        //System.err.print("FPS: " + 2 * current.sliceHeaders.get(0).sps.vuiParams.time_scale);
        return current;
    }

    static InnerAccessUnit decodedPoc(InnerAccessUnit current, InnerAccessUnit previous) {
        insertDecodingTime(current, previous);
        switch (current.sliceHeaders.get(0).sps.pic_order_cnt_type) {
            case 0:
                return decodePocType0(current, previous);
            case 1:
                return decodePocType1(current, previous);
            case 2:
                return decodePocType2(current, previous);
            default:
                return null;
        }

    }


    public void setFps(double fps) {
        this.fps = fps;
    }
}
