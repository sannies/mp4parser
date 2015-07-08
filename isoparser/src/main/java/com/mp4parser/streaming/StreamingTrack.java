package com.mp4parser.streaming;

import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TrackHeaderBox;

import java.util.concurrent.BlockingQueue;

public interface StreamingTrack {
    long getTimescale();

    BlockingQueue<StreamingSample> getSamples();

    /**
     * Returns false if and only if the BlockingQueue returned by getSamples() is empty
     * and the streams source is depleted.
     *
     * @return false if we can stop processing
     */
    boolean hasMoreSamples();

    /**
     * Returns the original TrackHeaderBox. Changes on the returned box should always
     * be visible. Do not return a copy or create on the fly.
     * @return the original TrackHeaderBox
     */
    TrackHeaderBox getTrackHeaderBox();

    String getHandler();

    String getLanguage();

    SampleDescriptionBox getSampleDescriptionBox();

    TrackExtension[] getExtensions();


}
