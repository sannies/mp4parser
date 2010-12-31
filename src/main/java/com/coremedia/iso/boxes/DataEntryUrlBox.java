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

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * Only used within the DataReferenceBox. Find more information there.
 *
 * @see com.coremedia.iso.boxes.DataReferenceBox
 */
public class DataEntryUrlBox extends FullBox {
    public static final String TYPE = "url ";

    private String location;

    public DataEntryUrlBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLocation() {
        return location;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        if ((getFlags() & 0x1) != 0x1) {
            throw new UnsupportedOperationException();
        }
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        if ((getFlags() & 0x1) != 0x1) {
            throw new UnsupportedOperationException();
        }
    }

    protected long getContentSize() {
        return 0;
    }

    public String getDisplayName() {
        return "Data Entry Url Box";
    }

    public String toString() {
        return "DataEntryUrlBox[location=" + getLocation() + "]";
    }
}
