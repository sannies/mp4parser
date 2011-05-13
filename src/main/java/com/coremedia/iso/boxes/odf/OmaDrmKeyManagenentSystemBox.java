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

package com.coremedia.iso.boxes.odf;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullContainerBox;
import com.coremedia.iso.boxes.OmaDrmAccessUnitFormatBox;

import java.io.IOException;

/**
 * Signals OMA Key Management in a PDCF file.
 * There may be several instnaces of the 'odkm' box in a PDCF file, and one can appear either at the movie
 * level or exactly one per each rpotected track.
 */
public class OmaDrmKeyManagenentSystemBox extends FullContainerBox {
    public static final String TYPE = "odkm";

    public OmaDrmKeyManagenentSystemBox() {
        super(TYPE);
    }

    public String getDisplayName() {
        return "Oma Drm Key Managenent System Box";
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        assert boxes.get(0) instanceof OmaDrmCommonHeadersBox;
        assert boxes.size() == 1 || boxes.get(1) instanceof OmaDrmAccessUnitFormatBox;
    }

    public OmaDrmCommonHeadersBox getOmaDrmCommonHeadersBox() {
        return (OmaDrmCommonHeadersBox) boxes.get(0);
    }

    public OmaDrmAccessUnitFormatBox getDrmAccessUnitFormatBox() {
        if (boxes.size() < 2) {
            OmaDrmAccessUnitFormatBox box = new OmaDrmAccessUnitFormatBox();
            box.setAllBits((byte) 1);
            box.setKeyIndicatorLength(0);
            box.setInitVectorLength(16);
            return box;
        }
        return (OmaDrmAccessUnitFormatBox) boxes.get(1);
    }

    public void setOmaDrmCommonHeadersBox(OmaDrmCommonHeadersBox box) {
        boxes.set(0, box);
    }

    public void setDrmAccessUnitFormatBox(OmaDrmAccessUnitFormatBox box) {
        boxes.set(1, box);
    }


}
