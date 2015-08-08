/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.mp4parser.boxes.iso14496.part12;

import com.mp4parser.Container;
import com.mp4parser.support.AbstractContainerBox;
import com.mp4parser.tools.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * aligned(8) class MovieFragmentBox extends Box(moof){
 * }
 */

public class MovieFragmentBox extends AbstractContainerBox {
    public static final String TYPE = "moof";

    public MovieFragmentBox() {
        super(TYPE);
    }


    public List<Long> getSyncSamples(SampleDependencyTypeBox sdtp) {
        List<Long> result = new ArrayList<Long>();

        final List<SampleDependencyTypeBox.Entry> sampleEntries = sdtp.getEntries();
        long i = 1;
        for (SampleDependencyTypeBox.Entry sampleEntry : sampleEntries) {
            if (sampleEntry.getSampleDependsOn() == 2) {
                result.add(i);
            }
            i++;
        }

        return result;
    }


    public int getTrackCount() {
        return getBoxes(TrackFragmentBox.class, false).size();
    }

    /**
     * Returns the track numbers associated with this <code>MovieBox</code>.
     *
     * @return the tracknumbers (IDs) of the tracks in their order of appearance in the file
     */

    public long[] getTrackNumbers() {

        List<TrackFragmentBox> trackBoxes = this.getBoxes(TrackFragmentBox.class, false);
        long[] trackNumbers = new long[trackBoxes.size()];
        for (int trackCounter = 0; trackCounter < trackBoxes.size(); trackCounter++) {
            TrackFragmentBox trackBoxe = trackBoxes.get(trackCounter);
            trackNumbers[trackCounter] = trackBoxe.getTrackFragmentHeaderBox().getTrackId();
        }
        return trackNumbers;
    }

    public List<TrackFragmentHeaderBox> getTrackFragmentHeaderBoxes() {
        return Path.getPaths((Container) this, "tfhd");
    }

    public List<TrackRunBox> getTrackRunBoxes() {
        return getBoxes(TrackRunBox.class, true);
    }

}
