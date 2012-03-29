/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

import java.util.Arrays;

/**
 * This <code>FragmentIntersectionFinder</code> cuts the input movie exactly before
 * the sync samples. Each fragment starts with a sync sample.
 */
public class SyncSampleIntersectFinderImpl implements FragmentIntersectionFinder {
    public int[] sampleNumbers(Track track, Movie movie) {
        Track syncSampleContainingTrack = null;
        int syncSampleContainingTrackSampleCount = 0;
        long[] syncSamples = null;
        for (Track currentTrack : movie.getTracks()) {
            long[] currentTrackSyncSamples = currentTrack.getSyncSamples();

            if (currentTrackSyncSamples != null && currentTrackSyncSamples.length > 0) {
                if (syncSampleContainingTrack == null || Arrays.equals(syncSamples, currentTrackSyncSamples)) {
                    syncSampleContainingTrack = currentTrack;
                    syncSampleContainingTrackSampleCount = currentTrack.getSamples().size();
                    syncSamples = currentTrackSyncSamples;
                } else {
                    throw new RuntimeException("There is more than one track containing a Sync Sample Box but the algorithm cannot deal with it. What is the most important track?");
                }
            }
        }
        if (syncSampleContainingTrack == null) {
            throw new RuntimeException("There was no track containing a Sync Sample Box but the Sync Sample Box is required to determine the fragment size.");
        }

        int[] chunkSizes = new int[syncSamples.length];
        if (track.getSamples().size() == 1) {
            chunkSizes[0] = 0;
            for (int i = 1; i < chunkSizes.length; i++) {
                chunkSizes[i] = 1;
            }
        } else {
            long sc = track.getSamples().size();
            double stretch = (double) sc / syncSampleContainingTrackSampleCount;
            for (int i = 0; i < chunkSizes.length; i++) {
                int start = (int) Math.round(stretch * (syncSamples[i] - 1));
                chunkSizes[i] = start;
                // The Stretch makes sure that there are as much audio and video chunks!
            }
        }
        return chunkSizes;

    }
}
