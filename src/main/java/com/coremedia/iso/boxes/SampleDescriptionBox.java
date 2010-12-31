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
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * The sample description table gives detailed information about the coding type used, and any initialization
 * information needed for that coding. <br>
 * The information stored in the sample description box after the entry-count is both track-type specific as
 * documented here, and can also have variants within a track type (e.g. different codings may use different
 * specific information after some common fields, even within a video track).<br>
 * For video tracks, a VisualSampleEntry is used; for audio tracks, an AudioSampleEntry. Hint tracks use an
 * entry format specific to their protocol, with an appropriate name. Timed Text tracks use a TextSampleEntry
 * For hint tracks, the sample description contains appropriate declarative data for the streaming protocol being
 * used, and the format of the hint track. The definition of the sample description is specific to the protocol.
 * Multiple descriptions may be used within a track.<br>
 * The 'protocol' and 'codingname' fields are registered identifiers that uniquely identify the streaming protocol or
 * compression format decoder to be used. A given protocol or codingname may have optional or required
 * extensions to the sample description (e.g. codec initialization parameters). All such extensions shall be within
 * boxes; these boxes occur after the required fields. Unrecognized boxes shall be ignored.
 * <br>
 * Defined in ISO/IEC 14496-12
 *
 * @see com.coremedia.iso.boxes.sampleentry.VisualSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.TextSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.AudioSampleEntry
 * @see com.coremedia.iso.boxes.rtp.HintSampleEntry
 */
public class SampleDescriptionBox extends FullContainerBox {
    public static final String TYPE = "stsd";

    public SampleDescriptionBox() {
        super(TYPE);
    }

    protected long getContentSize() {
        long size = 4;
        for (BoxInterface box : boxes) {
            size += box.getSize();
        }
        return size;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        parseHeader(in, size);
        long entryCount = in.readUInt32();
        if (entryCount > Integer.MAX_VALUE) {
            throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE subboxes");
        }
        boxes = new Box[(int) entryCount];
        long sp = in.position();
        for (int i = 0; i < entryCount; i++) {
            boxes[i] = boxParser.parseBox(in, this, lastMovieFragmentBox);
        }

        if (in.position() - offset < size) {
            // System.out.println("dead bytes found in " + box);
            long length = (size - (in.position() - offset));
            setDeadBytes(in.getSegment(in.position(), length));
        }
    }

    public String getDisplayName() {
        return "Sample Description Box";
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt32(boxes.length);
        for (BoxInterface boxe : boxes) {
            boxe.getBox(isos);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("SampleDescriptionBox[");
        BoxInterface[] boxes = getBoxes();
        for (int i = 0; i < boxes.length; i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(boxes[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }
}
