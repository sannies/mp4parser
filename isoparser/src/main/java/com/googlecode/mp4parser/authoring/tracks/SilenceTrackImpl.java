package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SubSampleInformationBox;
import com.googlecode.mp4parser.authoring.*;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.GroupEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * This is just a basic idea how things could work but they don't.
 */
public class SilenceTrackImpl implements Track {
    Track source;

    List<Sample> samples = new LinkedList<Sample>();
    long[] decodingTimes;
    String name;

    public SilenceTrackImpl(Track ofType, long ms) {
        source = ofType;
        name = "" + ms + "ms silence";
        if ("mp4a".equals(ofType.getSampleDescriptionBox().getSampleEntry().getType())) {
            int numFrames = l2i(getTrackMetaData().getTimescale() * ms / 1000 / 1024);
            decodingTimes = new long[numFrames];
            Arrays.fill(decodingTimes, getTrackMetaData().getTimescale() * ms / numFrames / 1000);

            while (numFrames-- > 0) {
                samples.add(new SampleImpl((ByteBuffer) ByteBuffer.wrap(new byte[]{
                        0x21, 0x10, 0x04, 0x60, (byte) 0x8c, 0x1c,
                }).rewind()));
            }

        } else {
            throw new RuntimeException("Tracks of type " + ofType.getClass().getSimpleName() + " are not supported");
        }
    }

    public void close() throws IOException {
        // nothing to close
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return source.getSampleDescriptionBox();
    }

    public long[] getSampleDurations() {
        return decodingTimes;
    }

    public long getDuration() {
        long duration = 0;
        for (long delta : decodingTimes) {
            duration += delta;
        }
        return duration;
    }

    public TrackMetaData getTrackMetaData() {
        return source.getTrackMetaData();
    }

    public String getHandler() {
        return source.getHandler();
    }


    public List<Sample> getSamples() {
        return samples;
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return null;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return null;
    }

    public long[] getSyncSamples() {
        return null;
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return null;
    }

    public String getName() {
        return name;
    }

    public List<Edit> getEdits() {
        return null;
    }

    public Map<GroupEntry, long[]> getSampleGroups() {
        return source.getSampleGroups();
    }
}
