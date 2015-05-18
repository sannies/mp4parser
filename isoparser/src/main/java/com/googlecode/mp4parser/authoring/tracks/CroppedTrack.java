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
package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.*;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;

import java.io.IOException;
import java.util.*;

/**
 * Generates a Track that starts at fromSample and ends at toSample (exclusive). The user of this class
 * has to make sure that the fromSample is a random access sample.
 * <ul>
 * <li>In AAC and most other audio formats this is every single sample</li>
 * <li>In H264 this is every sample that is marked in the SyncSampleBox</li>
 * </ul>
 */
public class CroppedTrack extends AbstractTrack {
    Track origTrack;
    private int fromSample;
    private int toSample;

    /**
     * Wraps an existing track and masks out a number of samples.
     * Works like {@link java.util.List#subList(int, int)}.
     *
     * @param origTrack  the original <code>Track</code>
     * @param fromSample first sample in the new <code>Track</code> - beginning with 0
     * @param toSample   first sample not in the new <code>Track</code> - beginning with 0
     */
    public CroppedTrack(Track origTrack, long fromSample, long toSample) {
        super("crop(" + origTrack.getName() + ")");
        this.origTrack = origTrack;
        assert fromSample <= Integer.MAX_VALUE;
        assert toSample <= Integer.MAX_VALUE;
        this.fromSample = (int) fromSample;
        this.toSample = (int) toSample;
    }

    public void close() throws IOException {
        origTrack.close();
    }


    public List<Sample> getSamples() {
        return origTrack.getSamples().subList(fromSample, toSample);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return origTrack.getSampleDescriptionBox();
    }

    public synchronized long[] getSampleDurations() {
        long[] decodingTimes = new long[toSample - fromSample];
        System.arraycopy(origTrack.getSampleDurations(), fromSample, decodingTimes, 0, decodingTimes.length);
        return decodingTimes;
    }

    static List<TimeToSampleBox.Entry> getDecodingTimeEntries(List<TimeToSampleBox.Entry> origSamples, long fromSample, long toSample) {
        if (origSamples != null && !origSamples.isEmpty()) {
            long current = 0;
            ListIterator<TimeToSampleBox.Entry> e = origSamples.listIterator();
            LinkedList<TimeToSampleBox.Entry> nuList = new LinkedList<TimeToSampleBox.Entry>();

            // Skip while not yet reached:
            TimeToSampleBox.Entry currentEntry;
            while ((currentEntry = e.next()).getCount() + current <= fromSample) {
                current += currentEntry.getCount();
            }
            // Take just a bit from the next
            if (currentEntry.getCount() + current >= toSample) {
                nuList.add(new TimeToSampleBox.Entry(toSample - fromSample, currentEntry.getDelta()));
                return nuList; // done in one step
            } else {
                nuList.add(new TimeToSampleBox.Entry(currentEntry.getCount() + current - fromSample, currentEntry.getDelta()));
            }
            current += currentEntry.getCount();

            while (e.hasNext() && (currentEntry = e.next()).getCount() + current < toSample) {
                nuList.add(currentEntry);
                current += currentEntry.getCount();
            }

            nuList.add(new TimeToSampleBox.Entry(toSample - current, currentEntry.getDelta()));

            return nuList;
        } else {
            return null;
        }
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return getCompositionTimeEntries(origTrack.getCompositionTimeEntries(), fromSample, toSample);
    }

    static List<CompositionTimeToSample.Entry> getCompositionTimeEntries(List<CompositionTimeToSample.Entry> origSamples, long fromSample, long toSample) {
        if (origSamples != null && !origSamples.isEmpty()) {
            long current = 0;
            ListIterator<CompositionTimeToSample.Entry> e = origSamples.listIterator();
            ArrayList<CompositionTimeToSample.Entry> nuList = new ArrayList<CompositionTimeToSample.Entry>();

            // Skip while not yet reached:
            CompositionTimeToSample.Entry currentEntry;
            while ((currentEntry = e.next()).getCount() + current <= fromSample) {
                current += currentEntry.getCount();
            }
            // Take just a bit from the next
            if (currentEntry.getCount() + current >= toSample) {
                nuList.add(new CompositionTimeToSample.Entry((int) (toSample - fromSample), currentEntry.getOffset()));
                return nuList; // done in one step
            } else {
                nuList.add(new CompositionTimeToSample.Entry((int) (currentEntry.getCount() + current - fromSample), currentEntry.getOffset()));
            }
            current += currentEntry.getCount();

            while (e.hasNext() && (currentEntry = e.next()).getCount() + current < toSample) {
                nuList.add(currentEntry);
                current += currentEntry.getCount();
            }

            nuList.add(new CompositionTimeToSample.Entry((int) (toSample - current), currentEntry.getOffset()));

            return nuList;
        } else {
            return null;
        }
    }

    synchronized public long[] getSyncSamples() {
        if (origTrack.getSyncSamples() != null) {
            long[] origSyncSamples = origTrack.getSyncSamples();
            int i = 0, j = origSyncSamples.length;
            while (i < origSyncSamples.length && origSyncSamples[i] < fromSample) {
                i++;
            }
            while (j > 0 && toSample < origSyncSamples[j - 1]) {
                j--;
            }
            long[] syncSampleArray = new long[j - i];
            System.arraycopy(origTrack.getSyncSamples(), i, syncSampleArray, 0, j - i);
            for (int k = 0; k < syncSampleArray.length; k++) {
                syncSampleArray[k] -= fromSample;
            }
            return syncSampleArray;
        }
        return null;
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        if (origTrack.getSampleDependencies() != null && !origTrack.getSampleDependencies().isEmpty()) {
            return origTrack.getSampleDependencies().subList(fromSample, toSample);
        } else {
            return null;
        }
    }

    public TrackMetaData getTrackMetaData() {
        return origTrack.getTrackMetaData();
    }

    public String getHandler() {
        return origTrack.getHandler();
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return origTrack.getSubsampleInformationBox();
    }

}
