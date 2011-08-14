package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TimeToSampleBox;

import java.util.List;

/**
 * Represents a Track. A track is a timed sequence of related samples.
 * <p/>
 * <b>NOTE: </b><br/
 * For media data, a track corresponds to a sequence of images or sampled audio; for hint tracks, a track
 * corresponds to a streaming channel.
 */
public interface Track {
    List<IsoBufferWrapper> getSamples();

    SampleDescriptionBox getSampleDescriptionBox();

    List<TimeToSampleBox.Entry> getDecodingTimeEntries();

    List<CompositionTimeToSample.Entry> getCompositionTimeEntries();

    long[] getSyncSamples();

    List<SampleDependencyTypeBox.Entry> getSampleDependencies();

    TrackMetaData getTrackMetaData();

    Type getType();

    boolean isEnabled();

    boolean isInMovie();

    boolean isInPreview();

    boolean isInPoster();

    public enum Type {
        VIDEO(),
        HINT(),
        TEXT(),
        NULL(),
        SOUND(),
        UNKNOWN()
    }
}
