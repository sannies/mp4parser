package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SubSampleInformationBox;
import com.googlecode.mp4parser.authoring.Edit;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.GroupEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class BetterFragmenterTest {

    public void verify(long additional, long[] in, long[] expectedOut) {
        Track t = new DummyTrack(additional, in);
        Fragmenter f = new BetterFragmenter(2.0);
        long[] segmentStarter = f.sampleNumbers(t);
        Assert.assertArrayEquals(expectedOut, segmentStarter);
    }

    @Test
    public void testPatterns() throws Exception {
        verify(5, new long[]{1, 51, 62, 101}, new long[]{1, 51});
        verify(50, new long[]{1, 6, 52, 101}, new long[]{1, 52, 101});
        verify(50, new long[]{1, 51, 62, 101}, new long[]{1, 51, 101});
    }

    class DummyTrack implements Track {

        long[] syncSamples;
        long[] sampleDurations;

        public DummyTrack(long additional, long... syncSamples) {
            this.syncSamples = syncSamples;
            int lastSample = (int) (syncSamples[syncSamples.length - 1] + additional);
            sampleDurations = new long[lastSample];
            Arrays.fill(sampleDurations, 40);


        }

        public SampleDescriptionBox getSampleDescriptionBox() {
            return null;
        }

        public long[] getSampleDurations() {
            return sampleDurations;
        }

        public long getDuration() {
            long duration = 0;
            for (long delta : getSampleDurations()) {
                duration += delta;
            }
            return duration;
        }

        public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
            return null;
        }

        public long[] getSyncSamples() {

            return syncSamples;
        }


        public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
            return null;
        }


        public TrackMetaData getTrackMetaData() {
            TrackMetaData tmd = new TrackMetaData();
            tmd.setTimescale(1000);
            return tmd;
        }


        public String getHandler() {
            return null;
        }


        public List<Sample> getSamples() {
            return null;
        }


        public SubSampleInformationBox getSubsampleInformationBox() {
            return null;
        }


        public String getName() {
            return null;
        }


        public List<Edit> getEdits() {
            return null;
        }


        public Map<GroupEntry, long[]> getSampleGroups() {
            return null;
        }


        public void close() throws IOException {

        }
    }
}