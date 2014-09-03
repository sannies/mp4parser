package com.googlecode.mp4parser.authoring;

/**
 * Format agnostic EditListBox.Entry.
 */
public class Edit {
    public Edit(long timeScale, long segmentDuration, long mediaTime, double mediaRate) {
        this.timeScale = timeScale;
        this.segmentDuration = segmentDuration;
        this.mediaTime = mediaTime;
        this.mediaRate = mediaRate;
    }

    private long timeScale;
    private long segmentDuration;
    private long mediaTime;
    private double mediaRate;

    public long getTimeScale() {
        return timeScale;
    }

    public long getSegmentDuration() {
        return segmentDuration;
    }

    public long getMediaTime() {
        return mediaTime;
    }

    public double getMediaRate() {
        return mediaRate;
    }
}
