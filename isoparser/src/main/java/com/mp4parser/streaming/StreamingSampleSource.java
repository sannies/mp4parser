package com.mp4parser.streaming;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TrackHeaderBox;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public interface StreamingSampleSource {
    boolean isSyncSampleAware();

    boolean isCttsAware();

    long getTimescale();

    BlockingQueue<StreamingSample> getSamples();

    /**
     * Returns false if and only if the BlockingQueue returned by getSamples() is empty
     * and the streams source is depleted.
     *
     * @return false if we can stop processing
     */
    boolean hasMoreSamples();

    TrackHeaderBox getTrackHeaderBox();

    String getHandler();

    String getLanguage();

    SampleDescriptionBox getSampleDescriptionBox();
}
