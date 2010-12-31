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

package com.coremedia.iso.boxes.vodafone;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * A vodafone specific box.
 */
public class CoverUriBox extends AbstractFullBox {
    public static final String TYPE = "cvru";

    private String coverUri;

    public CoverUriBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }

    public String getDisplayName() {
        return "Cover URI Box";
    }

    protected long getContentSize() {
        return utf8StringLengthInBytes(coverUri) + 1;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);   // 4 bytes are parsed in here
        coverUri = in.readString((int) (size - 4));
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeStringZeroTerm(coverUri);
    }


    public String toString() {
        return "CoverUriBox[coverUri=" + getCoverUri() + "]";
    }
}
