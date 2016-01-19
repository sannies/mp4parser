/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.Box;
import org.mp4parser.support.AbstractContainerBox;
import org.mp4parser.tools.Path;

import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * Tracks are used for two purposes: (a) to contain media data (media tracks) and (b) to contain packetization
 * information for streaming protocols (hint tracks).  <br>
 * There shall be at least one media track within an ISO file, and all the media tracks that contributed to the hint
 * tracks shall remain in the file, even if the media data within them is not referenced by the hint tracks; after
 * deleting all hint tracks, the entire un-hinted presentation shall remain.
 */
public class TrackBox extends AbstractContainerBox {
    public static final String TYPE = "trak";
    private SampleTableBox sampleTableBox;

    public TrackBox() {
        super(TYPE);
    }

    public TrackHeaderBox getTrackHeaderBox() {
        return Path.getPath(this, "tkhd[0]");
    }

    /**
     * Gets the SampleTableBox at mdia/minf/stbl if existing.
     *
     * @return the SampleTableBox or <code>null</code>
     */
    public SampleTableBox getSampleTableBox() {
        if (sampleTableBox != null) {
            return sampleTableBox;
        }
        MediaBox mdia = getMediaBox();
        if (mdia != null) {
            MediaInformationBox minf = mdia.getMediaInformationBox();
            if (minf != null) {
                sampleTableBox = minf.getSampleTableBox();
                return sampleTableBox;
            }
        }
        return null;

    }


    public MediaBox getMediaBox() {
        return Path.getPath(this, "mdia[0]");
    }

    @Override
    public void setBoxes(List<? extends Box> boxes) {
        super.setBoxes(boxes);
        sampleTableBox = null;
    }

}
