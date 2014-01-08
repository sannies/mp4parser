package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentIntersectionFinder;

import java.lang.Override;import java.util.Map;

/**
 * Uses a predefined list of sample numbers to divide up a track.
 */
public class StaticFragmentIntersectionFinderImpl implements FragmentIntersectionFinder {
    Map<Track, long[]> sampleNumbers;

    public StaticFragmentIntersectionFinderImpl(Map<Track, long[]> sampleNumbers) {
        this.sampleNumbers = sampleNumbers;
    }

    public long[] sampleNumbers(Track track) {
        return sampleNumbers.get(track);
    }
}
