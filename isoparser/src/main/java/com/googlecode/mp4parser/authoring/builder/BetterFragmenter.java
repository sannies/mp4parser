package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.util.Mp4Arrays;

import static java.util.Arrays.binarySearch;

/**
 * Created by sannies on 26.03.2016.
 */
public class BetterFragmenter implements Fragmenter {
    private double targetDuration;

    public BetterFragmenter(double targetDuration) {
        this.targetDuration = targetDuration;
    }

    public long[] sampleNumbers(Track track) {
        long ts = track.getTrackMetaData().getTimescale();
        long targetTicks = (long) (targetDuration * ts);
        long[] fragments = new long[0];
        long[] syncSamples = track.getSyncSamples();
        long[] durations = track.getSampleDurations();
        if (syncSamples != null) {
            long[] syncSampleTicks = new long[syncSamples.length];
            long ticks = 0;
            long duration = track.getDuration();

            for (int i = 0; i < durations.length; i++) {
                int pos = binarySearch(syncSamples, (long) i + 1);
                if (pos >= 0) {
                    syncSampleTicks[pos] = ticks;
                }
                ticks += durations[i];
            }
            long nextTargetTick = 0;

            for (int currentSyncSampleIndex = 0; currentSyncSampleIndex < syncSampleTicks.length - 1; currentSyncSampleIndex++) {
                long tickN1 = syncSampleTicks[currentSyncSampleIndex];
                long tickN2 = syncSampleTicks[currentSyncSampleIndex + 1];
                if (nextTargetTick <= tickN2) {
                    if (Math.abs(tickN1 - nextTargetTick) < Math.abs(tickN2 - nextTargetTick)) {
                        fragments = Mp4Arrays.copyOfAndAppend(fragments, syncSamples[currentSyncSampleIndex]);
                        nextTargetTick = syncSampleTicks[currentSyncSampleIndex] + targetTicks;
                    }
                }
            }
            if (duration - syncSampleTicks[syncSampleTicks.length - 1] > targetTicks / 2) {
                fragments = Mp4Arrays.copyOfAndAppend(fragments, syncSamples[syncSampleTicks.length - 1]);
            }
        } else {


            double time = 0.0D;
            fragments = new long[]{1L};
            for (int i = 0; i < durations.length; ++i) {
                time += (double) durations[i] / (double) ts;
                if (time >= targetDuration) {
                    if (i > 0) {
                        fragments = Mp4Arrays.copyOfAndAppend(fragments, (long) (i + 1));
                    }

                    time = 0.0D;
                }
            }
            // In case the last Fragment is shorter: make the previous one a bigger and omit the small one
            if (time < targetDuration && fragments.length > 1) {
                long[] nuSegmentStartSamples = new long[fragments.length - 1];
                System.arraycopy(fragments, 0, nuSegmentStartSamples, 0, fragments.length - 1);
                fragments = nuSegmentStartSamples;
            }

        }
        return fragments;

    }
}
