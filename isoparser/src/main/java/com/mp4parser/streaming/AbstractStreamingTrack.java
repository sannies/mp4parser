package com.mp4parser.streaming;

import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TrackHeaderBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractStreamingTrack implements StreamingTrack {
    protected BlockingQueue<StreamingSample> samples = new ArrayBlockingQueue<StreamingSample>(1000);
    protected TrackHeaderBox tkhd;
    protected SampleDescriptionBox stsd;
    protected HashMap<Class<? extends TrackExtension>, TrackExtension> trackExtensions = new HashMap<Class<? extends TrackExtension>, TrackExtension>();

    public AbstractStreamingTrack() {
        tkhd = new TrackHeaderBox();
        tkhd.setTrackId(1);
    }

    public BlockingQueue<StreamingSample> getSamples() {
        return samples;
    }

    public boolean hasMoreSamples() {
        return false;
    }

    public TrackHeaderBox getTrackHeaderBox() {
        return tkhd;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return stsd;
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
