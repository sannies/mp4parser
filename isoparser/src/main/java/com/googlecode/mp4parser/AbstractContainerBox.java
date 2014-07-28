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

package com.googlecode.mp4parser;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;


/**
 * Abstract base class suitable for most boxes acting purely as container for other boxes.
 */
public class AbstractContainerBox extends BasicContainer implements Box {

    Container parent;
    protected String type;
    protected boolean largeBox;
    private long offset;


    public AbstractContainerBox(String type) {
        this.type = type;
    }

    public Container getParent() {
        return parent;
    }

    public long getOffset() {
        return offset;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public long getSize() {
        long s = getContainerSize();
        return s + ((largeBox || (s + 8) >= (1L << 32)) ? 16 : 8);
    }

    public String getType() {
        return type;
    }

    protected ByteBuffer getHeader() {
        ByteBuffer header;
        if (largeBox || getSize() >= (1L << 32)) {
            header = ByteBuffer.wrap(new byte[]{0, 0, 0, 1, type.getBytes()[0], type.getBytes()[1], type.getBytes()[2], type.getBytes()[3], 0, 0, 0, 0, 0, 0, 0, 0});
            header.position(8);
            IsoTypeWriter.writeUInt64(header, getSize());
        } else {
            header = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, type.getBytes()[0], type.getBytes()[1], type.getBytes()[2], type.getBytes()[3]});
            IsoTypeWriter.writeUInt32(header, getSize());
        }
        header.rewind();
        return header;
    }

    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        this.offset = dataSource.position() - header.remaining();
        this.largeBox = header.remaining() == 16; // sometime people use large boxes without requiring them
        initContainer(dataSource, contentSize, boxParser);
    }


    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        writeContainer(writableByteChannel);
    }

    public void initContainer(DataSource dataSource, long containerSize, BoxParser boxParser) throws IOException {
        this.dataSource = dataSource;
        this.parsePosition = dataSource.position();
        this.startPosition =  parsePosition - (((largeBox || (containerSize + 8) >= (1L << 32)) ? 16 : 8));
        dataSource.position(dataSource.position() + containerSize);
        this.endPosition = dataSource.position();
        this.boxParser = boxParser;
    }

}
