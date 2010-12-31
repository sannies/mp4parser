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
 * The hint media header contains general information, independent of the protocaol, for hint tracks. Resides
 * in Media Information Box.
 *
 * @see com.coremedia.iso.boxes.MediaInformationBox
 */
public class HintMediaHeaderBox extends AbstractFullBox {
    private int maxPduSize;
    private int avgPduSize;
    private long maxBitrate;
    private long avgBitrate;
    public static final String TYPE = "hmhd";

    public HintMediaHeaderBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public int getMaxPduSize() {
        return maxPduSize;
    }

    public int getAvgPduSize() {
        return avgPduSize;
    }

    public long getMaxBitrate() {
        return maxBitrate;
    }

    public long getAvgBitrate() {
        return avgBitrate;
    }

    protected long getContentSize() {
        return 16;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        maxPduSize = in.readUInt16();
        avgPduSize = in.readUInt16();
        maxBitrate = in.readUInt32();
        avgBitrate = in.readUInt32();
        in.readUInt32();    // reserved!
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt16(maxPduSize);
        isos.writeUInt16(avgPduSize);
        isos.writeUInt32(maxBitrate);
        isos.writeUInt32(avgBitrate);
        isos.writeUInt32(0);

    }

    public String getDisplayName() {
        return "Hint Media Header Box";
    }

    public String toString() {
        return "HintMediaHeaderBox[]";
    }
}
