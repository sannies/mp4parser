package com.mp4parser.muxer.builder;

import com.mp4parser.muxer.Track;

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
