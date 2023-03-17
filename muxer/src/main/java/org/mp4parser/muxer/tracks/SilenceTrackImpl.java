package org.mp4parser.muxer.tracks;

import org.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import org.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import org.mp4parser.boxes.iso14496.part12.SubSampleInformationBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.muxer.*;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 * This is just a basic idea how things could work but they don't.
 */
public class SilenceTrackImpl implements Track {
    private Track source;

    private List<Sample> samples = new LinkedList<Sample>();
    private long[] decodingTimes;
    private String name;

    public SilenceTrackImpl(Track ofType, long ms) {

        source = ofType;
        name = "" + ms + "ms silence";
        assert ofType.getSampleEntries().size() == 1: "";
        if ("mp4a".equals(ofType.getSampleEntries().get(0).getType())) {
            int numFrames = l2i(getTrackMetaData().getTimescale() * ms / 1000 / 1024);
            if (numFrames <= 0) {
                return;
            }
            decodingTimes = new long[numFrames];
            Arrays.fill(decodingTimes, getTrackMetaData().getTimescale() * ms / numFrames / 1000);

            while (numFrames-- > 0) {
                samples.add(new SampleImpl((ByteBuffer) ((Buffer)ByteBuffer.wrap(new byte[]{
                        0x21, 0x10, 0x04, 0x60, (byte) 0x8c, 0x1c,
                })).rewind(), ofType.getSampleEntries().get(0)));
            }
        } else {
            throw new RuntimeException("Tracks of type " + ofType.getClass().getSimpleName() + " are not supported");
        }
    }

    public void close() throws IOException {
        // nothing to close
    }

    public List<SampleEntry> getSampleEntries() {
        return source.getSampleEntries();
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
