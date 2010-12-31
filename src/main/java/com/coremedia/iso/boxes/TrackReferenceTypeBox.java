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
 * Contains a reference to a track. The type of the box gives the kind of reference.
 */
public class TrackReferenceTypeBox extends AbstractBox {

    public static final String TYPE1 = "hint";
    public static final String TYPE2 = "cdsc";

    private long[] trackIds;

    public TrackReferenceTypeBox(byte[] type) {
        super(type);
    }

    public long[] getTrackIds() {
        return trackIds;
    }

    public String getDisplayName() {
        return "Track Reference Type Box";
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        int count = (int) (size / 4);
        trackIds = new long[count];
        for (int i = 0; i < count; i++) {
            trackIds[i] = in.readUInt32();

        }
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        for (long trackId : trackIds) {
            os.writeUInt32(trackId);
        }
    }

    protected long getContentSize() {
        return trackIds.length * 4;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TrackReferenceTypeBox[type=").append(IsoFile.bytesToFourCC(getType()));
        for (int i = 0; i < trackIds.length; i++) {
            buffer.append(";trackId");
            buffer.append(i);
            buffer.append("=");
            buffer.append(trackIds[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }
}
