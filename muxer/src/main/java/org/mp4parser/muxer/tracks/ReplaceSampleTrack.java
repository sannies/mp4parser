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
package org.mp4parser.muxer.tracks;

import org.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import org.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import org.mp4parser.boxes.iso14496.part12.SubSampleInformationBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.List;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 * Generates a Track where a single sample has been replaced by a given <code>ByteBuffer</code>.
 */

public class ReplaceSampleTrack extends AbstractTrack {
    Track origTrack;
    private long sampleNumber;
    private Sample sampleContent;
    private List<Sample> samples;

    public ReplaceSampleTrack(Track origTrack, long sampleNumber, ByteBuffer content) {
        super("replace(" + origTrack.getName() + ")");
        this.origTrack = origTrack;
        this.sampleNumber = sampleNumber;
        this.sampleContent = new SampleImpl(content, this.origTrack.getSamples().get(l2i(sampleNumber)).getSampleEntry());
        this.samples = new ReplaceASingleEntryList();

    }

    public void close() throws IOException {
        origTrack.close();
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public List<SampleEntry> getSampleEntries() {
        return origTrack.getSampleEntries();
    }

    public synchronized long[] getSampleDurations() {
        return origTrack.getSampleDurations();
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return origTrack.getCompositionTimeEntries();

    }

    synchronized public long[] getSyncSamples() {
        return origTrack.getSyncSamples();
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return origTrack.getSampleDependencies();
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

    private class ReplaceASingleEntryList extends AbstractList<Sample> {
        @Override
        public Sample get(int index) {
            if (ReplaceSampleTrack.this.sampleNumber == index) {
                return ReplaceSampleTrack.this.sampleContent;
            } else {
                return ReplaceSampleTrack.this.origTrack.getSamples().get(index);
            }
        }

        @Override
        public int size() {
            return ReplaceSampleTrack.this.origTrack.getSamples().size();
        }
    }

}
