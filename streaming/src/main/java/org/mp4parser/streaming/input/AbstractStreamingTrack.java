package org.mp4parser.streaming.input;

import org.mp4parser.boxes.iso14496.part12.TrackHeaderBox;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.TrackExtension;
import org.mp4parser.streaming.output.SampleSink;

import java.util.HashMap;

public abstract class AbstractStreamingTrack implements StreamingTrack {
    protected TrackHeaderBox tkhd;
    protected HashMap<Class<? extends TrackExtension>, TrackExtension> trackExtensions = new HashMap<Class<? extends TrackExtension>, TrackExtension>();

    protected SampleSink sampleSink;

    public AbstractStreamingTrack() {
        tkhd = new TrackHeaderBox();
        tkhd.setTrackId(1);
    }

    public void setSampleSink(SampleSink sampleSink) {
        this.sampleSink = sampleSink;
    }


    public <T extends TrackExtension> T getTrackExtension(Class<T> clazz) {
        return (T) trackExtensions.get(clazz);
    }

    public void addTrackExtension(TrackExtension trackExtension) {

        trackExtensions.put(trackExtension.getClass(), trackExtension);
    }

    public void removeTrackExtension(Class<? extends TrackExtension> clazz) {
        trackExtensions.remove(clazz);
    }
}
