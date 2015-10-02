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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * The Item Protection Box provides an array of item protection information, for use by the Item Information Box.
 *
 * @see ItemProtectionBox
 */
public class ItemProtectionBox extends AbstractContainerBox implements FullBox {

    public static final String TYPE = "ipro";
    private int version;
    private int flags;

    public ItemProtectionBox() {
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

    public SchemeInformationBox getItemProtectionScheme() {
        if (!getBoxes(SchemeInformationBox.class).isEmpty()) {
            return getBoxes(SchemeInformationBox.class).get(0);
        } else {
            return null;
        }
    }


    @Override
    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {

        ByteBuffer versionFlagNumOfChildBoxes = ByteBuffer.allocate(6);
        dataSource.read(versionFlagNumOfChildBoxes);
        versionFlagNumOfChildBoxes.rewind();
        version = IsoTypeReader.readUInt8(versionFlagNumOfChildBoxes);
        flags = IsoTypeReader.readUInt24(versionFlagNumOfChildBoxes);
        // number of child boxes is not required
        initContainer(dataSource, contentSize - 6, boxParser);
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer versionFlagNumOfChildBoxes = ByteBuffer.allocate(6);
        IsoTypeWriter.writeUInt8(versionFlagNumOfChildBoxes, version);
        IsoTypeWriter.writeUInt24(versionFlagNumOfChildBoxes, flags);
        IsoTypeWriter.writeUInt16(versionFlagNumOfChildBoxes, getBoxes().size());
        writableByteChannel.write((ByteBuffer) versionFlagNumOfChildBoxes.rewind());
        writeContainer(writableByteChannel);
    }


    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 6; // bytes to container start
        return s + t + ((largeBox || (s + t) >= (1L << 32)) ? 16 : 8);
    }
}
