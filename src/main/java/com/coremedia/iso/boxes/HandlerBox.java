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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This box within a Media Box declares the process by which the media-data in the track is presented,
 * and thus, the nature of the media in a track.
 * This Box when present in a Meta Box, declares the structure or format of the 'meta' box contents.
 * See ISO/IEC 14496-12 for details.
 *
 * @see MetaBox
 * @see MediaBox
 */
public class HandlerBox extends AbstractFullBox {
    public static final String TYPE = "hdlr";
    public static final Map<String, String> readableTypes;

    static {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("odsm", "ObjectDescriptorStream - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");
        hm.put("crsm", "ClockReferenceStream - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");
        hm.put("sdsm", "SceneDescriptionStream - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");
        hm.put("m7sm", "MPEG7Stream - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");
        hm.put("ocsm", "ObjectContentInfoStream - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");
        hm.put("ipsm", "IPMP Stream - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");
        hm.put("mjsm", "MPEG-J Stream - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");
        hm.put("mdir", "Apple Meta Data iTunes Reader");
        hm.put("mp7b", "MPEG-7 binary XML");
        hm.put("mp7t", "MPEG-7 XML");
        hm.put("vide", "Video Track");
        hm.put("soun", "Sound Track");
        hm.put("hint", "Hint Track");
        hm.put("appl", "Apple specific");
        hm.put("meta", "Timed Metadata track - defined in ISO/IEC JTC1/SC29/WG11 - CODING OF MOVING PICTURES AND AUDIO");

        readableTypes = Collections.unmodifiableMap(hm);

    }

    private String handlerType;
    private String name = null;
    private long a, b, c;
    private boolean zeroTerm = true;

    public HandlerBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getHandlerType() {
        return handlerType;
    }

    /**
     * You are required to add a '\0' string termination by yourself.
     *
     * @param name the new human readable name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setHandlerType(String handlerType) {
        this.handlerType = handlerType;
    }

    public String getName() {
        return name;
    }

    public String getHumanReadableTrackType() {
        return readableTypes.get(handlerType) != null ? readableTypes.get(handlerType) : "Unknown Handler Type";
    }

    public String getDisplayName() {
        return "Handler Reference Box";
    }

    protected long getContentSize() {
        if (zeroTerm) {
            return 21 + utf8StringLengthInBytes(name);
        } else {
            return 20 + utf8StringLengthInBytes(name);
        }

    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        in.readUInt32();
        handlerType = IsoFile.bytesToFourCC(in.read(4));
        a = in.readUInt32();
        b = in.readUInt32();
        c = in.readUInt32();
        if ((int) (size - 24) > 0) {
            name = in.readString((int) (size - 24));
            if (name.endsWith("\0")) {
                name = name.substring(0, name.length()-1);
                zeroTerm = true;
            } else {
                zeroTerm = false;
            }
        } else {
            zeroTerm = false; //No string at all, not even zero term char
        }
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt32(0);
        isos.write(IsoFile.fourCCtoBytes(handlerType));
        isos.writeUInt32(a);
        isos.writeUInt32(b);
        isos.writeUInt32(c);
        isos.writeStringNoTerm(name);
        if (zeroTerm) {
            isos.write(0);
        }
    }

    public String toString() {
        return "HandlerBox[handlerType=" + getHandlerType() + ";name=" + getName() + "]";
    }
}
