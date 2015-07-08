package com.mp4parser.streaming;

public class StreamingTrackHelper {
    static boolean hasTrackExtension(StreamingTrack streamingTrack, Class<? extends TrackExtension> clazz) {
        for (TrackExtension trackExtension : streamingTrack.getExtensions()) {
            if (clazz.isAssignableFrom(trackExtension.getClass())) {
                return true;
            }
        }
        return false;
    }

    static <B extends TrackExtension> B getTrackExtension(StreamingTrack streamingTrack, Class<B> clazz) {
        for (TrackExtension trackExtension : streamingTrack.getExtensions()) {
            if (clazz.isAssignableFrom(trackExtension.getClass())) {
                return (B) trackExtension;
            }
        }
        return null;
    }
}
