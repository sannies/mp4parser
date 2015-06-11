package com.mp4parser.streaming;

import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TrackHeaderBox;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sannies on 31.05.2015.
 */
public abstract class AbstractStreamingTrack implements StreamingTrack {
    protected BlockingQueue<StreamingSample> samples = new ArrayBlockingQueue<StreamingSample>(1000);
    protected TrackHeaderBox tkhd = new TrackHeaderBox();
    protected SampleDescriptionBox stsd = new SampleDescriptionBox();

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


    public TrackExtension[] getExtensions() {
        return new TrackExtension[0];
    }
}
