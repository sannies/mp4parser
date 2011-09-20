package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.MultiplexIsoBufferWrapperImpl;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.h264.AccessUnit;
import com.googlecode.mp4parser.h264.AnnexB2SampleDemuxer;

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
        RawH264Track track = new RawH264Track(new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-samurai_h640w_track1.h264")));
    }

    public RawH264Track(IsoBufferWrapperImpl rawH264) throws IOException {
        AnnexB2SampleDemuxer annexBDemuxer = new AnnexB2SampleDemuxer(rawH264);
        AccessUnit au;
        while ((au = annexBDemuxer.nextAcceessUnit()) != null) {
            List<IsoBufferWrapper> nals = new LinkedList<IsoBufferWrapper>();
            IsoBufferWrapper nalUnit;
            while ((nalUnit = au.nextNALUnit()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new IsoOutputStream(baos).writeUInt32(nalUnit.size());
                nals.add(new IsoBufferWrapperImpl(baos.toByteArray()));
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
