package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.boxes.AbstractMediaHeaderBox;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TimeToSampleBox;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Represents a Track. A track is a timed sequence of related samples.
 * <p/>
 * <b>NOTE: </b><br/
 * For media data, a track corresponds to a sequence of images or sampled audio; for hint tracks, a track
 * corresponds to a streaming channel.
 */
public interface Track {

    SampleDescriptionBox getSampleDescriptionBox();

    List<TimeToSampleBox.Entry> getDecodingTimeEntries();

    List<CompositionTimeToSample.Entry> getCompositionTimeEntries();

    long[] getSyncSamples();

    List<SampleDependencyTypeBox.Entry> getSampleDependencies();

    TrackMetaData getTrackMetaData();

    String getHandler();

    boolean isEnabled();

    boolean isInMovie();

    boolean isInPreview();

    boolean isInPoster();

    List<ByteBuffer> getSamples();

    public AbstractMediaHeaderBox getMediaHeaderBox();

}
