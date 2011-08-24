package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

/**
 *
 */
public interface FragmentIntersectionFinder {
    /**
     * Gets the ordinal number of the samples which will be the first sample
     * in each fragment.
     *
     * @param track concerned track
     * @param movie the context of the track
     * @return an array containing the ordinal of each fragment's first sample
     */
    public int[] sampleNumbers(Track track, Movie movie);
}
