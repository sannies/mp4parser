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
package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.boxes.*;

import java.util.List;

/**
 * <p>Represents a Track. A track is a timed sequence of related samples.</p>
 * <p/>
 * <b>NOTE: </b>
 * <p>For media data, a track corresponds to a sequence of images or sampled audio; for hint tracks, a track
 * corresponds to a streaming channel.</p>
 */
public interface Track {

    SampleDescriptionBox getSampleDescriptionBox();

    /**
     * Each samples is covers a small time span in a video. This method
     * returns the duration for each sample in track timescale. The array
     * must contain exactly as many samples as {@link #getSamples()} contains.
     * @return an array of ticks
     */
    long[] getSampleDurations();

    /**
     * The duration of the track in track timescale. It's typically the
     * sum of all samples' duration.
     * @return the track's duration
     */
    long getDuration();

    List<CompositionTimeToSample.Entry> getCompositionTimeEntries();

    long[] getSyncSamples();

    List<SampleDependencyTypeBox.Entry> getSampleDependencies();

    TrackMetaData getTrackMetaData();

    String getHandler();

    List<Sample> getSamples();

    public Box getMediaHeaderBox();

    public SubSampleInformationBox getSubsampleInformationBox();

}
