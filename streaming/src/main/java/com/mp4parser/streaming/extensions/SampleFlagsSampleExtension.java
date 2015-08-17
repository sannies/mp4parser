package com.mp4parser.streaming.extensions;

import com.mp4parser.streaming.SampleExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SampleFlagsSampleExtension implements SampleExtension {
    public static Map<Long, SampleFlagsSampleExtension> pool =
            Collections.synchronizedMap(new HashMap<Long, SampleFlagsSampleExtension>());

    private byte isLeading, sampleDependsOn, sampleIsDependedOn, sampleHasRedundancy, samplePaddingValue;
    private boolean sampleIsNonSyncSample;
    private int sampleDegradationPriority;

    public static SampleFlagsSampleExtension create(
            byte isLeading, byte sampleDependsOn, byte sampleIsDependedOn,
            byte sampleHasRedundancy, byte samplePaddingValue, boolean sampleIsNonSyncSample, int sampleDegradationPriority) {
        long key = isLeading + (sampleDependsOn << 2) + (sampleIsDependedOn << 4) + (sampleHasRedundancy << 6);
        key += (samplePaddingValue<<8);
        key += (sampleDegradationPriority<<11);
        key += (sampleIsNonSyncSample?1:0)<<27;

        SampleFlagsSampleExtension c = pool.get(key);
        if (c == null) {
            c = new SampleFlagsSampleExtension();
            c.isLeading = isLeading;
            c.sampleDependsOn = sampleDependsOn;
            c.sampleIsDependedOn = sampleIsDependedOn;
            c.sampleHasRedundancy = sampleHasRedundancy;
            c.samplePaddingValue = samplePaddingValue;
            c.sampleIsNonSyncSample = sampleIsNonSyncSample;
            c.sampleDegradationPriority = sampleDegradationPriority;
            pool.put(key, c);
        }
        return c;
    }


    public byte getIsLeading() {
        return isLeading;
    }

    public void setIsLeading(byte isLeading) {
        this.isLeading = isLeading;
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

    public void setSampleHasRedundancy(byte sampleHasRedundancy) {
        this.sampleHasRedundancy = sampleHasRedundancy;
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
