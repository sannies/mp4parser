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
package com.mp4parser.muxer.builder;

import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.boxes.iso14496.part12.OriginalFormatBox;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.sampleentry.AudioSampleEntry;
import com.mp4parser.tools.Path;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static com.mp4parser.tools.Mp4Math.lcm;

/**
 * This <code>FragmentIntersectionFinder</code> cuts the input movie video tracks in
 * fragments of the same length exactly before the sync samples. Audio tracks are cut
 * into pieces of similar length.
 */
public class SyncSampleIntersectFinderImpl implements Fragmenter {

    private static Logger LOG = Logger.getLogger(SyncSampleIntersectFinderImpl.class.getName());

    private final int minFragmentDurationSeconds;
    private Movie movie;
    private Track referenceTrack;

    /**
     * Creates a <code>SyncSampleIntersectFinderImpl</code> that will not create any fragment
     * smaller than the given <code>minFragmentDurationSeconds</code>
     *
     * @param movie this movie is the reference
     * @param referenceTrack used for audio tracks to find similar boundaries of segments.
     * @param minFragmentDurationSeconds the smallest allowable duration of a fragment.
     */
    public SyncSampleIntersectFinderImpl(Movie movie, Track referenceTrack, int minFragmentDurationSeconds) {
        this.movie = movie;
        this.referenceTrack = referenceTrack;
        this.minFragmentDurationSeconds = minFragmentDurationSeconds;
    }

    static String getFormat(Track track) {
        SampleDescriptionBox stsd = track.getSampleDescriptionBox();
        OriginalFormatBox frma = Path.getPath(stsd, "enc./sinf/frma");
        if (frma!=null) {
            return frma.getDataFormat();
        } else {
            return stsd.getSampleEntry().getType();
        }
    }

    /**
     * Calculates the timestamp of all tracks' sync samples.
     *
     * @param movie <code>track</code> is located in this movie
     * @param track get this track's samples timestamps
     * @return a list of timestamps
     */
    public static List<long[]> getSyncSamplesTimestamps(Movie movie, Track track) {
        List<long[]> times = new LinkedList<long[]>();
        for (Track currentTrack : movie.getTracks()) {
            if (currentTrack.getHandler().equals(track.getHandler())) {
                long[] currentTrackSyncSamples = currentTrack.getSyncSamples();
                if (currentTrackSyncSamples != null && currentTrackSyncSamples.length > 0) {
                    final long[] currentTrackTimes = getTimes(currentTrack, movie);
                    times.add(currentTrackTimes);
                }
            }
        }
        return times;
    }

    private static long[] getTimes(Track track, Movie m) {
        long[] syncSamples = track.getSyncSamples();
        long[] syncSampleTimes = new long[syncSamples.length];

        int currentSample = 1;  // first syncsample is 1
        long currentDuration = 0;
        int currentSyncSampleIndex = 0;

        final long scalingFactor = calculateTracktimesScalingFactor(m, track);

        while (currentSample <= syncSamples[syncSamples.length - 1]) {
            if (currentSample == syncSamples[currentSyncSampleIndex]) {
                syncSampleTimes[currentSyncSampleIndex++] = currentDuration * scalingFactor;
            }
            currentDuration += track.getSampleDurations()[-1 + currentSample++];
        }
        return syncSampleTimes;
    }

    private static long calculateTracktimesScalingFactor(Movie m, Track track) {
        long timeScale = 1;
        for (Track track1 : m.getTracks()) {
            if (track1.getHandler().equals(track.getHandler())) {
                if (track1.getTrackMetaData().getTimescale() != track.getTrackMetaData().getTimescale()) {
                    timeScale = lcm(timeScale, track1.getTrackMetaData().getTimescale());
                }
            }
        }
        return timeScale;
    }

    /**
     * Gets an array of sample numbers that are meant to be the first sample of each
     * chunk or fragment.
     *
     * @param track concerned track
     * @return an array containing the ordinal of each fragment's first sample
     */
    public long[] sampleNumbers(Track track) {

        if ("vide".equals(track.getHandler())) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                List<long[]> times = getSyncSamplesTimestamps(movie, track);
                return getCommonIndices(track.getSyncSamples(), getTimes(track, movie), track.getTrackMetaData().getTimescale(), times.toArray(new long[times.size()][]));
            } else {
                throw new RuntimeException("Video Tracks need sync samples. Only tracks other than video may have no sync samples.");
            }
        } else if ("soun".equals(track.getHandler())) {
            if (referenceTrack == null) {
                for (Track candidate : movie.getTracks()) {
                    if (candidate.getSyncSamples() != null && "vide".equals(candidate.getHandler()) && candidate.getSyncSamples().length > 0) {
                        referenceTrack = candidate;
                    }
                }
            }
            if (referenceTrack != null) {

                // Gets the reference track's fra
                long[] refSyncSamples = sampleNumbers(referenceTrack);

                int refSampleCount = referenceTrack.getSamples().size();

                long[] syncSamples = new long[refSyncSamples.length];
                long minSampleRate = 192000;
                for (Track testTrack : movie.getTracks()) {
                    if (getFormat(track).equals(getFormat(testTrack))) {
                        AudioSampleEntry ase = (AudioSampleEntry) testTrack.getSampleDescriptionBox().getSampleEntry();
                        if (ase.getSampleRate() < minSampleRate) {
                            minSampleRate = ase.getSampleRate();
                            long sc = testTrack.getSamples().size();
                            double stretch = (double) sc / refSampleCount;

                            long samplesPerFrame = testTrack.getSampleDurations()[0]; // Assuming all audio tracks have the same number of samples per frame, which they do for all known types

                            for (int i = 0; i < syncSamples.length; i++) {
                                long start = (long) Math.ceil(stretch * (refSyncSamples[i] - 1) * samplesPerFrame);
                                syncSamples[i] = start;
                                // The Stretch makes sure that there are as much audio and video chunks!
                            }
                            break;
                        }
                    }
                }
                AudioSampleEntry ase = (AudioSampleEntry) track.getSampleDescriptionBox().getSampleEntry();

                long samplesPerFrame = track.getSampleDurations()[0]; // Assuming all audio tracks have the same number of samples per frame, which they do for all known types
                double factor = (double) ase.getSampleRate() / (double) minSampleRate;
                if (factor != Math.rint(factor)) { // Not an integer
                    throw new RuntimeException("Sample rates must be a multiple of the lowest sample rate to create a correct file!");
                }
                for (int i = 0; i < syncSamples.length; i++) {
                    syncSamples[i] = (long) (1 + syncSamples[i] * factor / (double) samplesPerFrame);
                }
                return syncSamples;
            }
            throw new RuntimeException("There was absolutely no Track with sync samples. I can't work with that!");
        } else {
            // Ok, my track has no sync samples - let's find one with sync samples.
            for (Track candidate : movie.getTracks()) {
                if (candidate.getSyncSamples() != null && candidate.getSyncSamples().length > 0) {
                    long[] refSyncSamples = sampleNumbers(candidate);
                    int refSampleCount = candidate.getSamples().size();

                    long[] syncSamples = new long[refSyncSamples.length];
                    long sc = track.getSamples().size();
                    double stretch = (double) sc / refSampleCount;

                    for (int i = 0; i < syncSamples.length; i++) {
                        long start = (long) Math.ceil(stretch * (refSyncSamples[i] - 1)) + 1;
                        syncSamples[i] = start;
                        // The Stretch makes sure that there are as much audio and video chunks!
                    }
                    return syncSamples;
                }
            }
            throw new RuntimeException("There was absolutely no Track with sync samples. I can't work with that!");
        }


    }

    public long[] getCommonIndices(long[] syncSamples, long[] syncSampleTimes, long timeScale, long[]... otherTracksTimes) {
        List<Long> nuSyncSamples = new LinkedList<Long>();
        List<Long> nuSyncSampleTimes = new LinkedList<Long>();


        for (int i = 0; i < syncSampleTimes.length; i++) {
            boolean foundInEveryRef = true;
            for (long[] times : otherTracksTimes) {
                foundInEveryRef &= (Arrays.binarySearch(times, syncSampleTimes[i]) >= 0);
            }

            if (foundInEveryRef) {
                // use sample only if found in every other track.
                nuSyncSamples.add(syncSamples[i]);
                nuSyncSampleTimes.add(syncSampleTimes[i]);
            }
        }
        // We have two arrays now:
        // nuSyncSamples: Contains all common sync samples
        // nuSyncSampleTimes: Contains the times of all sync samples

        // Start: Warn user if samples are not matching!
        if (nuSyncSamples.size() < (syncSamples.length * 0.25)) {
            String log = "";
            log += String.format("%5d - Common:  [", nuSyncSamples.size());
            for (long l : nuSyncSamples) {
                log += (String.format("%10d,", l));
            }
            log += ("]");
            LOG.warning(log);
            log = "";

            log += String.format("%5d - In    :  [", syncSamples.length);
            for (long l : syncSamples) {
                log += (String.format("%10d,", l));
            }
            log += ("]");
            LOG.warning(log);
            LOG.warning("There are less than 25% of common sync samples in the given track.");
            throw new RuntimeException("There are less than 25% of common sync samples in the given track.");
        } else if (nuSyncSamples.size() < (syncSamples.length * 0.5)) {
            LOG.fine("There are less than 50% of common sync samples in the given track. This is implausible but I'm ok to continue.");
        } else if (nuSyncSamples.size() < syncSamples.length) {
            LOG.finest("Common SyncSample positions vs. this tracks SyncSample positions: " + nuSyncSamples.size() + " vs. " + syncSamples.length);
        }
        // End: Warn user if samples are not matching!


        List<Long> finalSampleList = new LinkedList<Long>();

        if (minFragmentDurationSeconds > 0) {
            // if minFragmentDurationSeconds is greater 0
            // we need to throw away certain samples.
            long lastSyncSampleTime = -1;
            Iterator<Long> nuSyncSamplesIterator = nuSyncSamples.iterator();
            Iterator<Long> nuSyncSampleTimesIterator = nuSyncSampleTimes.iterator();
            while (nuSyncSamplesIterator.hasNext() && nuSyncSampleTimesIterator.hasNext()) {
                long curSyncSample = nuSyncSamplesIterator.next();
                long curSyncSampleTime = nuSyncSampleTimesIterator.next();
                if (lastSyncSampleTime == -1 || (curSyncSampleTime - lastSyncSampleTime) / timeScale >= minFragmentDurationSeconds) {
                    finalSampleList.add(curSyncSample);
                    lastSyncSampleTime = curSyncSampleTime;
                }
            }
        } else {
            // the list of all samples is the final list of samples
            // since minFragmentDurationSeconds ist not used.
            finalSampleList = nuSyncSamples;
        }


        // transform the list to an array
        long[] finalSampleArray = new long[finalSampleList.size()];
        for (int i = 0; i < finalSampleArray.length; i++) {
            finalSampleArray[i] = finalSampleList.get(i);
        }
        return finalSampleArray;

    }

}
