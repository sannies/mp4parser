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
 * Describes the format of media access units in PDCF files.
 */
public final class OmaDrmAccessUnitFormatBox extends FullBox {
    public static final String TYPE = "odaf";

    private boolean selectiveEncryption;
    private byte allBits;

    private int keyIndicatorLength;
    private int initVectorLength;

    protected long getContentSize() {
        return 3;
    }

    public OmaDrmAccessUnitFormatBox() {
        super(IsoFile.fourCCtoBytes("odaf"));
    }

    public String getDisplayName() {
        return "OMA DRM Access Unit Format Box";
    }

    public boolean isSelectiveEncryption() {
        return selectiveEncryption;
    }

    public int getKeyIndicatorLength() {
        return keyIndicatorLength;
    }

    public int getInitVectorLength() {
        return initVectorLength;
    }

    public void setInitVectorLength(int initVectorLength) {
        this.initVectorLength = initVectorLength;
    }

    public void setKeyIndicatorLength(int keyIndicatorLength) {
        this.keyIndicatorLength = keyIndicatorLength;
    }

    public void setAllBits(byte allBits) {
        this.allBits = allBits;
        selectiveEncryption = (allBits & 0x80) == 0x80;
    }


    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt8(allBits);
        isos.writeUInt8(keyIndicatorLength);
        isos.writeUInt8(initVectorLength);
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        allBits = (byte) in.readUInt8();
        selectiveEncryption = (allBits & 0x80) == 0x80;
        keyIndicatorLength = in.readUInt8();
        initVectorLength = in.readUInt8();
    }

}
