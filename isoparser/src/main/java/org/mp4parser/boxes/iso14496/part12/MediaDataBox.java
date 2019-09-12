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
import org.mp4parser.ParsableBox;
import org.mp4parser.support.DoNotParseDetail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * This box contains the media data. In video tracks, this box would contain video frames. A presentation may
 * contain zero or more Media Data Boxes. The actual media data follows the type field; its structure is described
 * by the metadata (see {@link SampleTableBox}).<br>
 * In large presentations, it may be desirable to have more data in this box than a 32-bit size would permit. In this
 * case, the large variant of the size field is used.<br>
 * There may be any number of these boxes in the file (including zero, if all the media data is in other files). The
 * metadata refers to media data by its absolute offset within the file (see {@link StaticChunkOffsetBox});
 * so Media Data Box headers and free space may easily be skipped, and files without any box structure may
 * also be referenced and used.
 */
public final class MediaDataBox implements ParsableBox {
    public static final String TYPE = "mdat";
    private static Logger LOG = LoggerFactory.getLogger(MediaDataBox.class);
    ByteBuffer header;
    File dataFile;

    public MediaDataBox() {
    }

    public String getType() {
        return TYPE;
    }


    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write((ByteBuffer) ((Buffer)header).rewind());
        FileChannel fc = new FileInputStream(dataFile).getChannel();

        fc.transferTo(0, dataFile.lastModified(), writableByteChannel);
        fc.close();
    }

    public long getSize() {
        return header.limit() + dataFile.length();
    }

    /**
     * {@inheritDoc}
     */
    @DoNotParseDetail
    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        dataFile = File.createTempFile("MediaDataBox", super.toString());
        
        // make sure to clean up temp file
        dataFile.deleteOnExit();

        this.header = ByteBuffer.allocate(header.limit());
        this.header.put(header);
        RandomAccessFile raf = new RandomAccessFile(dataFile, "rw");
        try {
            raf.getChannel().transferFrom(dataSource, 0, contentSize);
        } finally {
            raf.close();
        }

    }


}
