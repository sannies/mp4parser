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
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.MemoryDataSourceImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static com.googlecode.mp4parser.util.CastUtils.l2i;


/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * A common base structure to contain general metadata. See ISO/IEC 14496-12 Ch. 8.44.1.
 */
public class MetaBox extends AbstractContainerBox {
    public static final String TYPE = "meta";

    private boolean isFullBox = true; // default is fullbox cause that's what ISO defines, simple box is apple specifc

    private int version;
    private int flags;

    public MetaBox() {
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
    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(l2i(contentSize));
        dataSource.read(bb);
        bb.position(4);
        String isHdlr = IsoTypeReader.read4cc(bb);
        if ("hdlr".equals(isHdlr)) {
            isFullBox = false;
            initContainer(new MemoryDataSourceImpl((ByteBuffer) bb.rewind()), contentSize, boxParser);
            // we got apple specifc box here
        } else {
            isFullBox = true;
            parseVersionAndFlags((ByteBuffer) bb.rewind());
            initContainer(new MemoryDataSourceImpl((ByteBuffer) bb.rewind()), contentSize - 4, boxParser);
        }

    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        if (isFullBox) {
            ByteBuffer bb = ByteBuffer.allocate(4);
            writeVersionAndFlags(bb);
            writableByteChannel.write((ByteBuffer) bb.rewind());
        }
        writeContainer(writableByteChannel);
    }
    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 0; // bytes to container start
        if (isFullBox) {
            t += 4;
        }
        return s + t + ((largeBox || (s + t) >= (1L << 32)) ? 16 : 8);

    }
}
