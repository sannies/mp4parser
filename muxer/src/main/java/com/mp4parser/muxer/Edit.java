package com.mp4parser.muxer;

/**
 * Format agnostic EditListBox.Entry.
 */
public class Edit {
    private long timeScale;
    private double segmentDuration;
    private long mediaTime;
    private double mediaRate;

    /**
     * @param mediaTime           time within the current track that is considered start time of this edit.
     * @param timeScale           time scale of the media time entry
     * @param segmentDurationInMs segment duration in seconds
     * @param mediaRate           when mediaRate is 1.0 the playback will be normal. When 2.0 it will be twice as fast.
     */
    public Edit(long mediaTime, long timeScale, double mediaRate, double segmentDurationInMs) {
        this.timeScale = timeScale;
        this.segmentDuration = segmentDurationInMs;
        this.mediaTime = mediaTime;
        this.mediaRate = mediaRate;
    }

    public long getTimeScale() {
        return timeScale;
    }

    public double getSegmentDuration() {
        return segmentDuration;
    }

    public long getMediaTime() {
        return mediaTime;
    }

    public double getMediaRate() {
        return mediaRate;
    }
}
