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
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * The InfoURL box ('infu') contains a URL where additional information can be found regarding the
 * Content Object. The device MAY obtain this information prior to using the RightsIssuerURL field
 * or after the Rights Object has been obtained.<br>
 * The value of the InfoURL MUST be a URL according
 * to [RFC2396] and MUST be an absolute identifier. It is a string encoded using UTF-8 characters,
 * continuing until the end of the box is reached.
 */
public class OmaDrmInfoUrlBox extends AbstractFullBox {
    public static final String TYPE = "infu";

    private String infoUrl;

    protected long getContentSize() {
        return utf8StringLengthInBytes(infoUrl);
    }

    public OmaDrmInfoUrlBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public String getDisplayName() {
        return "Info URL Box";
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        infoUrl = in.readString((int) (size - 4));
    }

    public String toString() {
        return "OmaDrmInfoUrlBox[infoUrl=" + getInfoUrl() + "]";
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeStringNoTerm(infoUrl);
    }
}
