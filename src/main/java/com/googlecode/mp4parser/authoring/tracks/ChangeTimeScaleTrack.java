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
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Changes the timescale of a track by wrapping the track.
 */
public class ChangeTimeScaleTrack implements Track {
    Track source;
    List<CompositionTimeToSample.Entry> ctts;
    List<TimeToSampleBox.Entry> tts;
    long timeScale;

    public ChangeTimeScaleTrack(Track source, long timeScale) {
        this.source = source;
        this.timeScale = timeScale;
        double timeScaleFactor = (double) timeScale / source.getTrackMetaData().getTimescale();
        ctts = adjustCtts(source.getCompositionTimeEntries(), timeScaleFactor);
        tts = adjustTts(source.getDecodingTimeEntries(), timeScaleFactor);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return source.getSampleDescriptionBox();
    }

    public List<TimeToSampleBox.Entry> getDecodingTimeEntries() {
        return tts;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return ctts;
    }

    public long[] getSyncSamples() {
        return source.getSyncSamples();
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return source.getSampleDependencies();
    }

    public TrackMetaData getTrackMetaData() {
        TrackMetaData trackMetaData = (TrackMetaData) source.getTrackMetaData().clone();
        trackMetaData.setTimescale(timeScale);
        return trackMetaData;
    }

    public String getHandler() {
        return source.getHandler();
    }

    public boolean isEnabled() {
        return source.isEnabled();
    }

    public boolean isInMovie() {
        return source.isInMovie();
    }

    public boolean isInPreview() {
        return source.isInPreview();
    }

    public boolean isInPoster() {
        return source.isInPoster();
    }

    public List<ByteBuffer> getSamples() {
        return source.getSamples();
    }


    static List<CompositionTimeToSample.Entry> adjustCtts(List<CompositionTimeToSample.Entry> source, double timeScaleFactor) {
        if (source != null) {
            List<CompositionTimeToSample.Entry> entries2 = new ArrayList<CompositionTimeToSample.Entry>(source.size());
            double deviation = 0;

            for (CompositionTimeToSample.Entry entry : source) {
                double d = timeScaleFactor * entry.getOffset() + deviation;
                int x = (int) Math.round(d);
                deviation = d - x;
                entries2.add(new CompositionTimeToSample.Entry(entry.getCount(), x));
            }
            return entries2;
        } else {
            return null;
        }
    }

    static List<TimeToSampleBox.Entry> adjustTts(List<TimeToSampleBox.Entry> source, double timeScaleFactor) {
        double deviation = 0;

        List<TimeToSampleBox.Entry> entries2 = new ArrayList<TimeToSampleBox.Entry>(source.size());

        for (TimeToSampleBox.Entry entry : source) {
            double d = timeScaleFactor * entry.getDelta() + deviation;
            long x = Math.round(d);
            deviation = d - x;
            entries2.add(new TimeToSampleBox.Entry(entry.getCount(), x));
        }
        return entries2;
    }

    public AbstractMediaHeaderBox getMediaHeaderBox() {
        return source.getMediaHeaderBox();
    }
    public SubSampleInformationBox getSubsampleInformationBox() {
        return source.getSubsampleInformationBox();
    }

}
