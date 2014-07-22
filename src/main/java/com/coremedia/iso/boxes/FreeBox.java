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
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.util.ChannelHelper;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.googlecode.mp4parser.DataSource;

import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * A free box. Just a placeholder to enable editing without rewriting the whole file.
 */
public class FreeBox implements Box {
    public static final String TYPE = "free";
    ByteBuffer data;
    List<Box> replacers = new LinkedList<Box>();
    private Container parent;
    private long offset;

    public FreeBox() {
        this.data = ByteBuffer.wrap(new byte[0]);
    }

    public FreeBox(int size) {
        this.data = ByteBuffer.allocate(size);
    }

    public long getOffset() {
        return offset;
    }

    public ByteBuffer getData() {
        if (data != null) {
            return (ByteBuffer) data.duplicate().rewind();
        } else {
            return null;
        }
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public void getBox(WritableByteChannel os) throws IOException {
        for (Box replacer : replacers) {
            replacer.getBox(os);
        }
        ByteBuffer header = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt32(header, 8 + data.limit());
        header.put(TYPE.getBytes());
        header.rewind();
        os.write(header);
        header.rewind();
        data.rewind();
        os.write(data);
        data.rewind();

    }

    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public long getSize() {
        long size = 8;
        for (Box replacer : replacers) {
            size += replacer.getSize();
        }
        size += data.limit();
        return size;
    }

    public String getType() {
        return TYPE;
    }

    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        this.offset = dataSource.position() - header.remaining();
        if (contentSize > 1024 * 1024) {
            // It's quite expensive to map a file into the memory. Just do it when the box is larger than a MB.
            data = dataSource.map(dataSource.position(), contentSize);
            dataSource.position(dataSource.position() + contentSize);
        } else {
            assert contentSize < Integer.MAX_VALUE;
            data = ByteBuffer.allocate(l2i(contentSize));
            dataSource.read(data);
        }
    }


    public void addAndReplace(Box box) {
        data.position(l2i(box.getSize()));
        data = data.slice();
        replacers.add(box);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FreeBox freeBox = (FreeBox) o;

        if (getData() != null ? !getData().equals(freeBox.getData()) : freeBox.getData() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}