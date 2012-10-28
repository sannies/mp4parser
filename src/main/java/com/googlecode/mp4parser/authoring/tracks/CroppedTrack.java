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
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Generates a Track that starts at fromSample and ends at toSample (exclusive). The user of this class
 * has to make sure that the fromSample is a random access sample.
 * <ul>
 * <li>In AAC this is every single sample</li>
 * <li>In H264 this is every sample that is marked in the SyncSampleBox</li>
 * </ul>
 */
public class CroppedTrack extends AbstractTrack {
    Track origTrack;
    private int fromSample;
    private int toSample;
    private long[] syncSampleArray;

    public CroppedTrack(Track origTrack, long fromSample, long toSample) {
        this.origTrack = origTrack;
        assert fromSample <= Integer.MAX_VALUE;
        assert toSample <= Integer.MAX_VALUE;
        this.fromSample = (int) fromSample;
        this.toSample = (int) toSample;
    }

    public List<ByteBuffer> getSamples() {
        return origTrack.getSamples().subList(fromSample, toSample);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return origTrack.getSampleDescriptionBox();
    }

    public List<TimeToSampleBox.Entry> getDecodingTimeEntries() {
        return getDecodingTimeEntries(origTrack.getDecodingTimeEntries(), fromSample, toSample);
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
                nuList.add(new CompositionTimeToSample.Entry((int) (currentEntry.getCount() + current - fromSample), currentEntry.getCount()));
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
        if (this.syncSampleArray == null) {
            if (origTrack.getSyncSamples() != null && origTrack.getSyncSamples().length > 0) {
                List<Long> syncSamples = new LinkedList<Long>();
                for (long l : origTrack.getSyncSamples()) {
                    if (l >= fromSample && l < toSample) {
                        syncSamples.add(l - fromSample);
                    }
                }
                syncSampleArray = new long[syncSamples.size()];
                for (int i = 0; i < syncSampleArray.length; i++) {
                    syncSampleArray[i] = syncSamples.get(i);

                }
                return syncSampleArray;
            } else {
                return null;
            }
        } else {
            return this.syncSampleArray;
        }
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

    public Box getMediaHeaderBox() {
        return origTrack.getMediaHeaderBox();
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return origTrack.getSubsampleInformationBox();
    }

}