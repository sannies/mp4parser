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

import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.util.Mp4Arrays;

import java.util.Arrays;

/**
 * Finds start samples within a given track so that:
 * <ul>
 * <li>Each segment is at least <code>fragmentLength</code> seconds long</li>
 * <li>The last segment might be almost twice the size of the rest</li>
 * </ul>
 */
public class DefaultFragmenterImpl implements Fragmenter {
    private double fragmentLength = 2.0D;

    public DefaultFragmenterImpl(double fragmentLength) {
        this.fragmentLength = fragmentLength;
    }

    public long[] sampleNumbers(Track track) {
        long[] segmentStartSamples = new long[]{1L};
        long[] sampleDurations = track.getSampleDurations();
        long[] syncSamples = track.getSyncSamples();
        long timescale = track.getTrackMetaData().getTimescale();
        double time = 0.0D;

        for (int i = 0; i < sampleDurations.length; ++i) {
            time += (double) sampleDurations[i] / (double) timescale;
            if (time >= this.fragmentLength && (syncSamples == null || Arrays.binarySearch(syncSamples, (long) (i + 1)) >= 0)) {
                if (i > 0) {
                    segmentStartSamples = Mp4Arrays.copyOfAndAppend(segmentStartSamples, (long) (i + 1));
                }

                time = 0.0D;
            }
        }
        // In case the last Fragment is shorter: make the previous one a bigger and omit the small one
        if (time < fragmentLength && segmentStartSamples.length > 1) {
            long[] nuSegmentStartSamples = new long[segmentStartSamples.length - 1];
            System.arraycopy(segmentStartSamples, 0, nuSegmentStartSamples, 0, segmentStartSamples.length - 1);
            segmentStartSamples = nuSegmentStartSamples;
        }

        return segmentStartSamples;
    }

}