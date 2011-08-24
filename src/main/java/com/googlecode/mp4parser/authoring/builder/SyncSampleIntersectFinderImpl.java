package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

/**
 *
 */
public class SyncSampleIntersectFinderImpl implements FragmentIntersectionFinder {
    public int[] sampleNumbers(Track track, Movie movie) {
        Track syncSampleContainingTrack = null;
        int syncSampleContainingTrackSampleCount = 0;
        long[] syncSamples = null;
        for (Track currentTrack : movie.getTracks()) {
            long[] currentTrackSyncSamples = currentTrack.getSyncSamples();

            if (currentTrackSyncSamples != null && currentTrackSyncSamples.length > 0) {
                if (syncSampleContainingTrack == null) {
                    syncSampleContainingTrack = currentTrack;
                    syncSampleContainingTrackSampleCount = track.getSamples().size();
                    syncSamples = currentTrackSyncSamples;
                } else {
                    throw new RuntimeException("There is more than one track containing a Sync Sample Box but the algorithm cannot deal with it. What is the most important track?");
                }
            }
        }
        if (syncSampleContainingTrack == null) {
            throw new RuntimeException("There was no track containing a Sync Sample Box but the Sync Sample Box is required to determine the fragment size.");
        }

        int numberOfFragments = 0;

        int[] chunkSizes = new int[syncSamples.length];
        long sc = track.getSamples().size();
        double stretch = (double) sc / syncSampleContainingTrackSampleCount;
        for (int i = 0; i < chunkSizes.length; i++) {
            int start = (int) Math.round(stretch * (syncSamples[i] - 1));
            chunkSizes[i] = start;
            // The Stretch makes sure that there are as much audio and video chunks!
        }
        numberOfFragments = chunkSizes.length;
        return chunkSizes;

    }
}
