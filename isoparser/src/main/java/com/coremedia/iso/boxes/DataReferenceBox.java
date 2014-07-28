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
import com.googlecode.mp4parser.AbstractContainerBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import com.googlecode.mp4parser.DataSource;
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
 * @see com.coremedia.iso.boxes.DataEntryUrlBox
 * @see com.coremedia.iso.boxes.DataEntryUrnBox
 */
public class DataReferenceBox extends AbstractContainerBox implements FullBox {

    public static final String TYPE = "dref";

    public DataReferenceBox() {
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
        writableByteChannel.write((ByteBuffer) versionFlagNumOfChildBoxes.rewind());
        writeContainer(writableByteChannel);
    }

    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 8;
        return s + t + ((largeBox || (s + t + 8) >= (1L << 32)) ? 16 : 8);

    }

}
