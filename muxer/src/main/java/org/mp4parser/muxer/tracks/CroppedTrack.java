package org.mp4parser.muxer.tracks;

import org.mp4parser.muxer.Track;

import java.util.List;

/**
 * @deprecated use ClippedTrack as "to crop" relates to a spatial dimension
 */
public class CroppedTrack extends ClippedTrack {
    /**
     * Wraps an existing track and masks out a number of samples.
     * Works like {@link List#subList(int, int)}.
     *
     * @param origTrack  the original <code>Track</code>
     * @param fromSample first sample in the new <code>Track</code> - beginning with 0
     * @param toSample   first sample not in the new <code>Track</code> - beginning with 0
     */
    public CroppedTrack(Track origTrack, long fromSample, long toSample) {
        super(origTrack, fromSample, toSample);
    }
}
