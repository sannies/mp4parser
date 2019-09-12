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

package org.mp4parser.boxes.iso14496.part12;


import org.mp4parser.BoxParser;
import org.mp4parser.FullBox;
import org.mp4parser.support.AbstractContainerBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * The data reference object contains a table of data references (normally URLs) that declare the location(s) of
 * the media data used within the presentation. The data reference index in the sample description ties entries in
 * this table to the samples in the track. A track may be split over several sources in this way.
 * If the flag is set indicating that the data is in the same file as this box, then no string (not even an empty one)
 * shall be supplied in the entry field.
 * The DataEntryBox within the DataReferenceBox shall be either a DataEntryUrnBox or a DataEntryUrlBox.
 *
 * @see DataEntryUrlBox
 * @see DataEntryUrnBox
 */
public class DataReferenceBox extends AbstractContainerBox implements FullBox {

    public static final String TYPE = "dref";
    private int version;
    private int flags;

    public DataReferenceBox() {
        super(TYPE);

    }

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
    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer versionFlagNumOfChildBoxes = ByteBuffer.allocate(8);
        int required = versionFlagNumOfChildBoxes.limit();
        while (required > 0){
            int read = dataSource.read(versionFlagNumOfChildBoxes);
            required -= read;
        }
        ((Buffer)versionFlagNumOfChildBoxes).rewind();
        version = IsoTypeReader.readUInt8(versionFlagNumOfChildBoxes);
        flags = IsoTypeReader.readUInt24(versionFlagNumOfChildBoxes);
        // number of child boxes is not required - ignore
        initContainer(dataSource, contentSize - 8, boxParser);
    }


    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer versionFlagNumOfChildBoxes = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt8(versionFlagNumOfChildBoxes, version);
        IsoTypeWriter.writeUInt24(versionFlagNumOfChildBoxes, flags);
        IsoTypeWriter.writeUInt32(versionFlagNumOfChildBoxes, getBoxes().size());
        writableByteChannel.write((ByteBuffer) ((Buffer)versionFlagNumOfChildBoxes).rewind());
        writeContainer(writableByteChannel);
    }

    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 8;
        return s + t + ((largeBox || (s + t + 8) >= (1L << 32)) ? 16 : 8);

    }

}
