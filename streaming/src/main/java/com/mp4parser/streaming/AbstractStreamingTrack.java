package com.mp4parser.streaming;

import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.iso14496.part12.TrackHeaderBox;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractStreamingTrack implements StreamingTrack {
    protected BlockingQueue<StreamingSample> samples = new ArrayBlockingQueue<StreamingSample>(1000);
    protected TrackHeaderBox tkhd;
    protected HashMap<Class<? extends TrackExtension>, TrackExtension> trackExtensions = new HashMap<Class<? extends TrackExtension>, TrackExtension>();

    public AbstractStreamingTrack() {
        tkhd = new TrackHeaderBox();
        tkhd.setTrackId(1);
    }

    public BlockingQueue<StreamingSample> getSamples() {
        return samples;
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
