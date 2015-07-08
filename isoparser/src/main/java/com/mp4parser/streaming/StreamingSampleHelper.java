package com.mp4parser.streaming;

public class StreamingSampleHelper {

    static boolean hasSampleExtension(StreamingSample streamingSample, Class<? extends SampleExtension> clazz) {
        for (SampleExtension sampleExtension : streamingSample.getExtensions()) {
            if (clazz.isAssignableFrom(sampleExtension.getClass())) {
                return true;
            }
        }
        return false;
    }

    static <B extends SampleExtension> B getSampleExtension(StreamingSample streamingSample, Class<B> clazz) {
        for (SampleExtension sampleExtension : streamingSample.getExtensions()) {
            if (clazz.isAssignableFrom(sampleExtension.getClass())) {
                return (B) sampleExtension;
            }
        }
        return null;
    }
}
