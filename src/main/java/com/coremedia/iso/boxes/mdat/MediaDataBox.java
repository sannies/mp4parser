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
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.googlecode.mp4parser.annotations.DoNotParseDetail;
import com.googlecode.mp4parser.util.ChannelHelper;
import com.googlecode.mp4parser.util.LazyList;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

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
    private static Logger LOG = Logger.getLogger(MediaDataBox.class.getName());

    public static final String TYPE = "mdat";
    public static final int BUFFER_SIZE = 10 * 1024 * 1024;
    ContainerBox parent;

    boolean largeBox = false;

    // These fields are for the special case of a FileChannel as input.
    private FileChannel fileChannel;
    private long startPosition;
    private long contentSize;


    /**
     * If the whole content is just in one mapped buffer keep a strong reference to it so it is
     * not evicted from the cache.
     */
    private ByteBuffer content;

    public ContainerBox getParent() {
        return parent;
    }

    public void setParent(ContainerBox parent) {
        this.parent = parent;
    }

    public String getType() {
        return TYPE;
    }

    private static void transfer(FileChannel from, long position, long count, WritableByteChannel to) throws IOException {
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
        if (fileChannel != null) {
            transfer(fileChannel, startPosition - (largeBox ? 16 : 8), contentSize + (largeBox ? 16 : 8), writableByteChannel);
        } else {
            ByteBuffer header;
            if (largeBox) {
                header = ByteBuffer.wrap(new byte[]{0, 0, 0, 1, 'm', 'd', 'a', 't', 0, 0, 0, 0, 0, 0, 0, 0});
                header.position(8);
                IsoTypeWriter.writeUInt64(header, contentSize + 16);
            } else {
                header = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 'm', 'd', 'a', 't'});
                IsoTypeWriter.writeUInt32(header, contentSize + 8);
            }
            header.rewind();
            writableByteChannel.write(header);
            writableByteChannel.write(content);
        }
    }


    public long getSize() {

        return (largeBox ? 16 : 8) + contentSize;
    }

    public long getDataStartPosition() {
        return startPosition;
    }

    public long getDataEndPosition() {
        return startPosition + contentSize;
    }

    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        this.largeBox = header.remaining() == 16;
        this.contentSize = contentSize;

        if (readableByteChannel instanceof FileChannel) {
            this.fileChannel = ((FileChannel) readableByteChannel);
            this.startPosition = ((FileChannel) readableByteChannel).position();
            ((FileChannel) readableByteChannel).position(((FileChannel) readableByteChannel).position() + contentSize);
        } else {
            content = ChannelHelper.readFully(readableByteChannel, l2i(contentSize));
            startPosition = 0;
            // this sucks I don't want to rely on these detailed knowledge of implementation details here.
            for (Box box : ((LazyList<Box>) this.getParent().getBoxes()).getUnderlying()) {
                startPosition += box.getSize();
            }
            startPosition += header.remaining();
            cacheSliceCurrentlyInUse = content;
            cacheSliceCurrentlyInUseStart = 0;
        }
    }

    ByteBuffer cacheSliceCurrentlyInUse = null;
    long cacheSliceCurrentlyInUseStart = Long.MAX_VALUE;

    public synchronized ByteBuffer getContent(long offset, int length) {
        // most likely the last used cache slice will be used again
        if (cacheSliceCurrentlyInUseStart <= offset && cacheSliceCurrentlyInUse != null && offset + length <= cacheSliceCurrentlyInUseStart + cacheSliceCurrentlyInUse.limit()) {
            ByteBuffer cachedSample = cacheSliceCurrentlyInUse.asReadOnlyBuffer();
            cachedSample.position((int) (offset - cacheSliceCurrentlyInUseStart));
            cachedSample.mark();
            cachedSample.limit((int) (offset - cacheSliceCurrentlyInUseStart) + length);
            return cachedSample;
        }

        // CACHE MISS

        try {
            // Just mapping 10MB at a time. Seems reasonable.
            cacheSliceCurrentlyInUse = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPosition + offset, Math.min(BUFFER_SIZE, contentSize - offset));
        } catch (IOException e1) {
            LOG.fine("Even mapping just 10MB of the source file into the memory failed. " + e1);
            throw new RuntimeException(
                    "Delayed reading of mdat content failed. Make sure not to close " +
                            "the FileChannel that has been used to create the IsoFile!", e1);
        }
        cacheSliceCurrentlyInUseStart = offset;
        ByteBuffer cachedSample = cacheSliceCurrentlyInUse.asReadOnlyBuffer();
        cachedSample.position(0);
        cachedSample.mark();
        cachedSample.limit(length);
        return cachedSample;
    }

    @Override
    public String toString() {
        return "MediaDataBox{" +
                "contentSize=" + contentSize +
                '}';
    }

    @DoNotParseDetail
    public String getPath() {
        return Path.createPath(this);
    }

}
