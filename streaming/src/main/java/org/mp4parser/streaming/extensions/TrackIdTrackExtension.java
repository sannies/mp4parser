package org.mp4parser.streaming.extensions;

import org.mp4parser.streaming.TrackExtension;

/**
 * Specifies the track ID of a track - if not set it's up to the StreamingMp4Writer to assume one.
 */
public class TrackIdTrackExtension implements TrackExtension {
    private long trackId = 1;

    public TrackIdTrackExtension(long trackId) {
        this.trackId = trackId;
    }

    public long getTrackId() {
        return trackId;
    }
}
