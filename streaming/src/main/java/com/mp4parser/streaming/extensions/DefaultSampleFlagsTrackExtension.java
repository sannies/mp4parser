package com.mp4parser.streaming.extensions;

import com.mp4parser.streaming.TrackExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sannies on 22.05.2015.
 */
public class DefaultSampleFlagsTrackExtension implements TrackExtension {
    public static Map<Long, SampleFlagsSampleExtension> pool =
            Collections.synchronizedMap(new HashMap<Long, SampleFlagsSampleExtension>());

    private byte isLeading, sampleDependsOn, sampleIsDependedOn, sampleHasRedundancy, samplePaddingValue;
    private boolean sampleIsNonSyncSample;
    private int sampleDegradationPriority;

    public static DefaultSampleFlagsTrackExtension create(
            byte isLeading, byte sampleDependsOn, byte sampleIsDependedOn,
            byte sampleHasRedundancy, byte samplePaddingValue, boolean sampleIsNonSyncSample, int sampleDegradationPriority) {

        DefaultSampleFlagsTrackExtension c = new DefaultSampleFlagsTrackExtension();
        c.isLeading = isLeading;
        c.sampleDependsOn = sampleDependsOn;
        c.sampleIsDependedOn = sampleIsDependedOn;
        c.sampleHasRedundancy = sampleHasRedundancy;
        c.samplePaddingValue = samplePaddingValue;
        c.sampleIsNonSyncSample = sampleIsNonSyncSample;
        c.sampleDegradationPriority = sampleDegradationPriority;
        return c;
    }


    public byte getIsLeading() {
        return isLeading;
    }

    public void setIsLeading(int isLeading) {
        this.isLeading = (byte)isLeading;
    }

    public byte getSampleDependsOn() {
        return sampleDependsOn;
    }

    public void setSampleDependsOn(int sampleDependsOn) {
        this.sampleDependsOn = (byte) sampleDependsOn;
    }

    public byte getSampleIsDependedOn() {
        return sampleIsDependedOn;
    }

    public void setSampleIsDependedOn(int sampleIsDependedOn) {
        this.sampleIsDependedOn = (byte) sampleIsDependedOn;
    }

    public byte getSampleHasRedundancy() {
        return sampleHasRedundancy;
    }

    public void setSampleHasRedundancy(int sampleHasRedundancy) {
        this.sampleHasRedundancy = (byte) sampleHasRedundancy;
    }

    public byte getSamplePaddingValue() {
        return samplePaddingValue;
    }

    public void setSamplePaddingValue(byte samplePaddingValue) {
        this.samplePaddingValue = samplePaddingValue;
    }

    public boolean isSampleIsNonSyncSample() {
        return sampleIsNonSyncSample;
    }

    public boolean isSyncSample() {
        return !sampleIsNonSyncSample;
    }

    public void setSampleIsNonSyncSample(boolean sampleIsNonSyncSample) {
        this.sampleIsNonSyncSample = sampleIsNonSyncSample;
    }

    public int getSampleDegradationPriority() {
        return sampleDegradationPriority;
    }

    public void setSampleDegradationPriority(int sampleDegradationPriority) {
        this.sampleDegradationPriority = sampleDegradationPriority;
    }
}
