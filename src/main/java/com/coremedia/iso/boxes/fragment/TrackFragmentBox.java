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

package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.BoxInterface;
import com.coremedia.iso.boxes.TrackMetaDataContainer;

/**
 * aligned(8) class TrackFragmentBox extends Box('traf'){
 * }
 */
public class TrackFragmentBox extends AbstractContainerBox implements TrackMetaDataContainer {
    public static final String TYPE = "traf";

    public TrackFragmentBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getDisplayName() {
        return "Track Fragment Box";
    }

    public TrackFragmentHeaderBox getTrackFragmentHeaderBox() {
        for (BoxInterface box : boxes) {
            if (box instanceof TrackFragmentHeaderBox) {
                return (TrackFragmentHeaderBox) box;
            }
        }
        return null;
    }
}
