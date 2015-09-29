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

package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.googlecode.mp4parser.DataSource;

import java.nio.channels.WritableByteChannel;
import java.util.logging.Logger;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * This box contains the media data. In video tracks, this box would contain video frames. A presentation may
 * contain zero or more Media Data Boxes. The actual media data follows the type field; its structure is described
 * by the metadata (see {@link com.coremedia.iso.boxes.SampleTableBox}).<br>
 * In large presentations, it may be desirable to have more data in this box than a 32-bit size would permit. In this
 * case, the large variant of the size field is used.<br>
 * There may be any number of these boxes in the file (including zero, if all the media data is in other files). The
 * metadata refers to media data by its absolute offset within the file (see {@link com.coremedia.iso.boxes.StaticChunkOffsetBox});
 * so Media Data Box headers and free space may easily be skipped, and files without any box structure may
 * also be referenced and used.
 */
public final class MediaDataBox implements Box {

    public static final String TYPE = "mdat";

    Container parent;

    // These fields are for the special case of a DataSource as input.
    private DataSource dataSource;
    private long offset;
    private long size;


    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public String getType() {
        return TYPE;
    }

    private static void transfer(DataSource from, long position, long count, WritableByteChannel to) throws IOException {
        long maxCount = (64 * 1024 * 1024) - (32 * 1024);
        // Transfer data in chunks a bit less than 64MB
        // People state that this is a kind of magic number on Windows.
        // I don't care. The size seems reasonable.
        long offset = 0;
        while (offset < count) {
            offset += from.transferTo(position + offset, Math.min(maxCount, count - offset), to);
        }
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        transfer(dataSource, offset, size, writableByteChannel);
    }


    public long getSize() {
        return size;
    }

    public long getOffset() {
        return offset;
    }

    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        this.offset = dataSource.position() - header.remaining();
        this.dataSource = dataSource;
        this.size = contentSize + header.remaining();
        dataSource.position(dataSource.position() + contentSize);

    }


    @Override
    public String toString() {
        return "MediaDataBox{" +
                "size=" + size +
                '}';
    }

}
