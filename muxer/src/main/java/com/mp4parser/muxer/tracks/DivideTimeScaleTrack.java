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
package com.mp4parser.muxer.tracks;

import com.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import com.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.iso14496.part12.SubSampleInformationBox;
import com.mp4parser.muxer.Edit;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.TrackMetaData;
import com.mp4parser.boxes.samplegrouping.GroupEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Changes the timescale of a track by wrapping the track.
 */
public class DivideTimeScaleTrack implements Track {
    Track source;
    private int timeScaleDivisor;

    public DivideTimeScaleTrack(Track source, int timeScaleDivisor) {
        this.source = source;
        this.timeScaleDivisor = timeScaleDivisor;
    }

    public void close() throws IOException {
        source.close();
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return source.getSampleDescriptionBox();
    }

    public long[] getSampleDurations() {
        long[] scaled = new long[source.getSampleDurations().length];


        for (int i = 0; i < source.getSampleDurations().length; i++) {
            scaled[i] = source.getSampleDurations()[i] / timeScaleDivisor;
        }
        return scaled;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return adjustCtts();
    }

    public long[] getSyncSamples() {
        return source.getSyncSamples();
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return source.getSampleDependencies();
    }

    public TrackMetaData getTrackMetaData() {
        TrackMetaData trackMetaData = (TrackMetaData) source.getTrackMetaData().clone();
        trackMetaData.setTimescale(source.getTrackMetaData().getTimescale() / this.timeScaleDivisor);
        return trackMetaData;
    }

    public String getHandler() {
        return source.getHandler();
    }


    public List<Sample> getSamples() {
        return source.getSamples();
    }


    List<CompositionTimeToSample.Entry> adjustCtts() {
        List<CompositionTimeToSample.Entry> origCtts = this.source.getCompositionTimeEntries();
        if (origCtts != null) {
            List<CompositionTimeToSample.Entry> entries2 = new ArrayList<CompositionTimeToSample.Entry>(origCtts.size());
            for (CompositionTimeToSample.Entry entry : origCtts) {
                entries2.add(new CompositionTimeToSample.Entry(entry.getCount(), entry.getOffset() / timeScaleDivisor));
            }
            return entries2;
        } else {
            return null;
        }
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return source.getSubsampleInformationBox();
    }

    public long getDuration() {
        long duration = 0;
        for (long delta : getSampleDurations()) {
            duration += delta;
        }
        return duration;
    }

    @Override
    public String toString() {
        return "MultiplyTimeScaleTrack{" +
                "source=" + source +
                '}';
    }

    public String getName() {
        return "timscale(" + source.getName() + ")";
    }

    public List<Edit> getEdits() {
        return source.getEdits();
    }

    public Map<GroupEntry, long[]> getSampleGroups() {
        return source.getSampleGroups();
    }
}
