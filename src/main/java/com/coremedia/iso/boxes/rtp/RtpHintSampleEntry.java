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

package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains basic information about the (rtp-) hint samples in this track.
 */
public class RtpHintSampleEntry extends SampleEntry implements ContainerBox {
    public static final String TYPE1 = "rtp ";

    private int hintTrackVersion;
    private int highestCompatibleVersion;
    private long maxPacketSize;

    public RtpHintSampleEntry(byte[] type) {
        super(type);
    }


    public int getHintTrackVersion() {
        return hintTrackVersion;
    }

    public int getHighestCompatibleVersion() {
        return highestCompatibleVersion;
    }

    public long getMaxPacketSize() {
        return maxPacketSize;
    }

    @Override
    protected long getContentSize() {
        long contentLength = 0;
        for (Box box : boxes) {
            contentLength += box.getSize();
        }
        return 16 + contentLength;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        hintTrackVersion = in.readUInt16();
        highestCompatibleVersion = in.readUInt16();
        maxPacketSize = in.readUInt32();
        size -= 16;

        while (size > 0) {
            Box box = boxParser.parseBox(in, this, lastMovieFragmentBox);
            size -= box.getSize();
            boxes.add(box);
        }
    }

    @Override
    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.write(new byte[6]);
        isos.writeUInt16(getDataReferenceIndex());
        isos.writeUInt16(hintTrackVersion);
        isos.writeUInt16(highestCompatibleVersion);
        isos.writeUInt32(maxPacketSize);
        for (Box boxe : boxes) {
            boxe.getBox(isos);
        }
    }

    @Override
    public String getDisplayName() {
        return "Hint Samply Entry";
    }

    public String toString() {
      StringBuilder builder = new StringBuilder();
        builder.append("RtpHintSampleEntry[");
        builder.append("hintTrackVersion=").append(getHintTrackVersion()).append(";");
        builder.append("highestCompatibleVersion=").append(getHighestCompatibleVersion()).append(";");
        builder.append("maxPacketSize=").append(getMaxPacketSize());
        for (int i = 0; i < boxes.size(); i++) {
            if (i > 0) {
                builder.append(";");
            }
            builder.append(boxes.get(i).toString());
        }
        builder.append("]");
        return builder.toString();
    }
}
