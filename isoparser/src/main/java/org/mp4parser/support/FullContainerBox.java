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

package org.mp4parser.support;

import org.mp4parser.Box;
import org.mp4parser.BoxParser;
import org.mp4parser.FullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for a full tools box only containing ither boxes.
 */
public abstract class FullContainerBox extends AbstractContainerBox implements FullBox {
    private static Logger LOG = LoggerFactory.getLogger(FullContainerBox.class);
    private int version;
    private int flags;

    public FullContainerBox(String type) {
        super(type);
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

    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    @Override
    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer versionAndFlags = ByteBuffer.allocate(4);
        dataSource.read(versionAndFlags);
        parseVersionAndFlags((ByteBuffer) versionAndFlags.rewind());
        super.parse(dataSource, header, contentSize, boxParser);
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        super.getBox(writableByteChannel);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[childBoxes]";
    }

    /**
     * Parses the version/flags header and returns the remaining box size.
     *
     * @param content the <code>ByteBuffer</code> that contains the version &amp; flag
     * @return number of bytes read
     */
    protected final long parseVersionAndFlags(ByteBuffer content) {
        version = IsoTypeReader.readUInt8(content);
        flags = IsoTypeReader.readUInt24(content);
        return 4;
    }

    protected final void writeVersionAndFlags(ByteBuffer bb) {
        IsoTypeWriter.writeUInt8(bb, version);
        IsoTypeWriter.writeUInt24(bb, flags);
    }

    @Override
    protected ByteBuffer getHeader() {
        ByteBuffer header;
        if (largeBox || getSize() >= (1L << 32)) {
            header = ByteBuffer.wrap(new byte[]{0, 0, 0, 1, type.getBytes()[0], type.getBytes()[1], type.getBytes()[2], type.getBytes()[3], 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
            header.position(8);
            IsoTypeWriter.writeUInt64(header, getSize());
            writeVersionAndFlags(header);
        } else {
            header = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, type.getBytes()[0], type.getBytes()[1], type.getBytes()[2], type.getBytes()[3], 0, 0, 0, 0});
            IsoTypeWriter.writeUInt32(header, getSize());
            header.position(8);
            writeVersionAndFlags(header);
        }
        header.rewind();
        return header;

    }

}
