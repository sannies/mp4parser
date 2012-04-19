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

import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.googlecode.mp4parser.util.Math.lcm;

/**
 * This <code>FragmentIntersectionFinder</code> cuts the input movie exactly before
 * the sync samples. Each fragment starts with a sync sample.
 */
public class SyncSampleIntersectFinderImpl implements FragmentIntersectionFinder {
    public long[] sampleNumbers(Track track, Movie movie) {


        if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
            List<long[]> times = new LinkedList<long[]>();
            for (Track currentTrack : movie.getTracks()) {
                if (currentTrack.getHandler().equals(track.getHandler())) {
                    long[] currentTrackSyncSamples = currentTrack.getSyncSamples();
                    if (currentTrackSyncSamples != null && currentTrackSyncSamples.length > 0) {
                        final long[] currentTrackTimes = getTimes(movie, currentTrack);
                        times.add(currentTrackTimes);
                    }
                }
            }

            long[] syncSamples = getCommonIndices(track.getSyncSamples(), getTimes(movie, track), times.toArray(new long[times.size()][]));
            if (syncSamples.length < (track.getSyncSamples().length * 0.3)) {
                throw new RuntimeException("There are less than 25% of common sync samples in the given track." + track);
            } else if (syncSamples.length < (track.getSyncSamples().length * 0.5)) {
                System.err.println("There are less than 50% of common sync samples in the given track. This is implausible but I'm ok to continue." + track);
            } else if (syncSamples.length < track.getSyncSamples().length) {
                System.err.println("Common SyncSample positions vs. this tracks SyncSample positions: " + syncSamples.length + " vs. " + track.getSyncSamples().length + " Track " + track);
            }
            return syncSamples;
        } else if ("vide".equals(track.getHandler())) {
            throw new RuntimeException("Video Tracks need sync samples. Only tracks other than video may have no sync samples.");
        } else {
            // Ok, my track has no sync samples - let's find one with sync samples.
            for (Track candidate : movie.getTracks()) {
                if (candidate.getSyncSamples() != null && candidate.getSyncSamples().length > 0) {
                    long[] refSyncSamples = sampleNumbers(candidate, movie);
                    int refSampleCount = candidate.getSamples().size();

                    long[] syncSamples = new long[refSyncSamples.length];
                    long sc = track.getSamples().size();
                    double stretch = (double) sc / refSampleCount;

                    for (int i = 0; i < syncSamples.length; i++) {
                        int start = (int) Math.ceil(stretch * (refSyncSamples[i] - 1)) + 1;
                        syncSamples[i] = start;
                        // The Stretch makes sure that there are as much audio and video chunks!
                    }
                    return syncSamples;
                }
            }
            throw new RuntimeException("There was absolutely no Track with sync samples. I can't work with that!");
        }


    }

    public static long[] getCommonIndices(long[] syncSamples, long[] syncSampleTimes, long[]... otherTracksTimes) {
        List<Long> nuSyncSamples = new LinkedList<Long>();
        for (int i = 0; i < syncSampleTimes.length; i++) {
            boolean foundInEveryRef = true;
            for (long[] times : otherTracksTimes) {
                foundInEveryRef &= (Arrays.binarySearch(times, syncSampleTimes[i]) >= 0);
            }
            if (foundInEveryRef) {
                nuSyncSamples.add(syncSamples[i]);
            }
        }
        long[] nuSyncSampleArray = new long[nuSyncSamples.size()];
        for (int i = 0; i < nuSyncSampleArray.length; i++) {
            nuSyncSampleArray[i] = nuSyncSamples.get(i);
        }
        return nuSyncSampleArray;
    }


    private static long[] getTimes(Movie m, Track track) {
        long[] syncSamples = track.getSyncSamples();
        long[] syncSampleTimes = new long[syncSamples.length];
        Queue<TimeToSampleBox.Entry> timeQueue = new LinkedList<TimeToSampleBox.Entry>(track.getDecodingTimeEntries());

        int currentSample = 1;  // first syncsample is 1
        long currentDuration = 0;
        long currentDelta = 0;
        int currentSyncSampleIndex = 0;
        long left = 0;

        long timeScale = 1;
        for (Track track1 : m.getTracks()) {
            if (track1.getTrackMetaData().getTimescale() != track.getTrackMetaData().getTimescale()) {
                timeScale = lcm(timeScale, track1.getTrackMetaData().getTimescale());
            }
        }


        while (currentSample <= syncSamples[syncSamples.length - 1]) {
            if (currentSample++ == syncSamples[currentSyncSampleIndex]) {
                syncSampleTimes[currentSyncSampleIndex++] = currentDuration * timeScale;
            }
            if (left-- == 0) {
                TimeToSampleBox.Entry entry = timeQueue.poll();
                left = entry.getCount();
                currentDelta = entry.getDelta();
            }
            currentDuration += currentDelta;
        }
        return syncSampleTimes;

    }
}
