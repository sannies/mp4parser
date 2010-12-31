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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Base class for all ISO Full boxes.
 */
public abstract class AbstractFullBox extends AbstractBox implements FullBox {
    private int version;
    private int flags;

    protected AbstractFullBox(byte[] type) {
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

    /**
     * Gets the box's content size without header size where header includes
     * flags and version.
     *
     * @return Gets the box's content size
     */
    protected abstract long getContentSize();

    @Override
    protected long getHeaderSize() {
        return super.getHeaderSize() + 4;
    }

    @Override
    public byte[] getHeader() {
        try {
            //TODO this is nearly identical to overriden method
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IsoOutputStream ios = new IsoOutputStream(baos);
            if (this.getSize() < 4294967296L) {
                ios.writeUInt32((int) this.getSize());
                ios.write(getType());
            } else {
                ios.writeUInt32(1);
                ios.write(getType());
                ios.writeUInt64(getSize());
            }
            if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("uuid"))) {
                ios.write(getUserType());
            }
            ios.writeUInt8(version);
            ios.writeUInt24(flags);

            assert baos.size() == getHeaderSize();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        parseHeader(in, size);
    }

    protected void parseHeader(IsoBufferWrapper in, long size) throws IOException {
        version = in.readUInt8();
        flags = in.readUInt24();
    }
}
