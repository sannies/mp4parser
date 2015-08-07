package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Track;

import java.util.Map;

/**
 * Uses a predefined list of sample numbers to divide up a track.
 */
public class StaticFragmentIntersectionFinderImpl implements Fragmenter {
    Map<Track, long[]> sampleNumbers;

    public StaticFragmentIntersectionFinderImpl(Map<Track, long[]> sampleNumbers) {
        this.sampleNumbers = sampleNumbers;
    }

    public long[] sampleNumbers(Track track) {
        return sampleNumbers.get(track);
    }
}
