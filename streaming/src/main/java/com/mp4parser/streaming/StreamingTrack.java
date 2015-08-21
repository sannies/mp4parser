package com.mp4parser.streaming;

import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.iso14496.part12.TrackHeaderBox;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;

public interface StreamingTrack extends Closeable {
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
     *
     * @return the original TrackHeaderBox
     */
    //TrackHeaderBox getTrackHeaderBox();

    String getHandler();

    String getLanguage();

    SampleDescriptionBox getSampleDescriptionBox();

    <T extends TrackExtension> T getTrackExtension(Class<T> clazz);

    void addTrackExtension(TrackExtension trackExtension);

    void removeTrackExtension(Class<? extends TrackExtension> clazz);

}
