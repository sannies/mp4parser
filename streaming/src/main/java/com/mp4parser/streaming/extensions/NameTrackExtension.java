package com.mp4parser.streaming.extensions;

import com.mp4parser.streaming.TrackExtension;

/**
 * Gives a track a name.
 */
public class NameTrackExtension implements TrackExtension {
    private String name;

    public static NameTrackExtension create(String name) {
        NameTrackExtension nameTrackExtension = new NameTrackExtension();
        nameTrackExtension.name = name;
        return nameTrackExtension;
    }

    public String getName() {
        return name;
    }
}
