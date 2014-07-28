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
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.sampleentry.AbstractSampleEntry;
import com.googlecode.mp4parser.AbstractContainerBox;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.googlecode.mp4parser.DataSource;

import java.nio.channels.WritableByteChannel;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
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
 */
public class SampleDescriptionBox extends AbstractContainerBox implements FullBox {
    public static final String TYPE = "stsd";

    public SampleDescriptionBox() {
        super(TYPE);
    }

    private int version;
    private int flags;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer versionFlagNumOfChildBoxes = ByteBuffer.allocate(8);
        dataSource.read(versionFlagNumOfChildBoxes);
        versionFlagNumOfChildBoxes.rewind();
        version = IsoTypeReader.readUInt8(versionFlagNumOfChildBoxes);
        flags = IsoTypeReader.readUInt24(versionFlagNumOfChildBoxes);
        // number of child boxes is not required
        initContainer(dataSource, contentSize - 8, boxParser);
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer versionFlagNumOfChildBoxes = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt8(versionFlagNumOfChildBoxes, version);
        IsoTypeWriter.writeUInt24(versionFlagNumOfChildBoxes, flags);
        IsoTypeWriter.writeUInt32(versionFlagNumOfChildBoxes, getBoxes().size());
        writableByteChannel.write((ByteBuffer) versionFlagNumOfChildBoxes.rewind());
        writeContainer(writableByteChannel);
    }

    public AbstractSampleEntry getSampleEntry() {
        for (AbstractSampleEntry box : getBoxes(AbstractSampleEntry.class)) {
            return box;
        }
        return null;
    }

    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 8;
        return s + t + ((largeBox || (s + t + 8) >= (1L << 32)) ? 16 : 8);

    }
}
