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
 * The rights object box MAY be used to insert a Protected Rights Object, defined in [DRM-v2] section 5.3.7, i
 * nto a DCF or PDCF. A free space box MAY include zero or more Rrights Oobject boxes. The Rights Object is
 * treated as binary data and a Device MAY add or delete Rights Object boxes in the parent free space box.
 */
public class OmaDrmRightsObjectBox extends AbstractFullBox {
    public static final String TYPE = "odrb";

    private byte[] rightsObject;

    public OmaDrmRightsObjectBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public byte[] getRightsObject() {
        return rightsObject;
    }

    protected long getContentSize() {
        return rightsObject.length;
    }

    public void setRightsObject(byte[] rightsObject) {
        this.rightsObject = rightsObject;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        rightsObject = in.read((int) size - 4);
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.write(rightsObject);
    }

    public String toString() {
        return "OmaDrmRightsObjectBox[rightsObject=" + getRightsObject() + "]";
    }
}
