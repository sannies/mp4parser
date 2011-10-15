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
import com.googlecode.mp4parser.h264.AccessUnit;
import com.googlecode.mp4parser.h264.AccessUnitSourceImpl;
import com.googlecode.mp4parser.h264.AnnexBNALUnitReader;
import com.googlecode.mp4parser.h264.NALUnitReader;
import com.googlecode.mp4parser.h264.model.NALUnit;
import com.googlecode.mp4parser.h264.model.NALUnitType;
import com.googlecode.mp4parser.h264.model.SEI;
import com.googlecode.mp4parser.h264.model.SliceHeader;
import com.googlecode.mp4parser.h264.model.SliceType;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.h264.read.SliceHeaderReader;
import com.googlecode.mp4parser.util.Path;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class RawH264Track extends AbstractTrack {
    private static int a;
    List<IsoBufferWrapper> samples = new ArrayList<IsoBufferWrapper>();


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
        NALUnitReader nalUnitReader = new AnnexBNALUnitReader(rawH264);
        AccessUnitSourceImpl accessUnitSource = new AccessUnitSourceImpl(nalUnitReader);
        SliceHeaderReader sliceHeaderReader = new SliceHeaderReader(accessUnitSource);
        AccessUnit au;
        while ((au = accessUnitSource.nextAccessUnit()) != null) {
            //System.err.println("Start AU");
            List<IsoBufferWrapper> nals = new LinkedList<IsoBufferWrapper>();
            List<SliceHeader> sliceObject = new LinkedList<SliceHeader>();
            IsoBufferWrapper nalUnit;
            SliceType sliceType = null;
            while ((nalUnit = au.nextNALUnit()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new IsoOutputStream(baos).writeUInt32(nalUnit.size());
                nals.add(new IsoBufferWrapperImpl(baos.toByteArray()));
                NALUnit nal = NALUnit.read(nalUnit);
                System.err.println(nal);
                if (nal.type == NALUnitType.IDR_SLICE || nal.type == NALUnitType.NON_IDR_SLICE) {
                    //System.err.print(nal + " --- ");
                    SliceHeader sliceHeader = sliceHeaderReader.read(nal, new CAVLCReader(nalUnit));
                    //System.err.println(sliceHeader);
                    sliceType = sliceHeader.slice_type;
                    sliceObject.add(sliceHeader);

                } else if (NALUnitType.SEI.equals(nal.type)) {
                    SEI sei = SEI.read(nalUnit);
                    System.err.println(sei);
                } else {
                    //System.err.println(nal);
                }

                nals.add(nalUnit);
                if (++a % 1000 == 0) {
                    System.err.println(a);
                }
            }
            samples.add(new MultiplexIsoBufferWrapperImpl(nals));
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

}
