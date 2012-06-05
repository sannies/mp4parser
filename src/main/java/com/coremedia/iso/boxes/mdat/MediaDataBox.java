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
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
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
    ContainerBox parent;

    ByteBuffer header;

    // Contains the memory mapped content of the mdat if memory mapping was successful
    // on 32 bit system memory mapping will fail for large files due to not enough
    // continuous memory
    private ByteBuffer content;


    // The next fields are required in case of failed memory mapping of the input file.
    public static boolean FAKE_MAPPING_FAIL = false;
    private FileChannel fileChannel;
    private long startPosition;
    private long contentSize;
    private Map<Long, SoftReference<ByteBuffer>> cache;

    public ContainerBox getParent() {
        return parent;
    }

    public void setParent(ContainerBox parent) {
        this.parent = parent;
    }

    public String getType() {
        return TYPE;
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        header.rewind();
        writableByteChannel.write(header);

        if (content != null) {
            content.rewind();
            writableByteChannel.write(content);
        } else {
            fileChannel.transferTo(startPosition, contentSize, writableByteChannel);
        }
    }

    public long getSize() {
        long size = header.limit();
        if (content != null) {
            size += content.limit();
        } else {
            size += contentSize;
        }
        return size;
    }

    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        this.header = header;
        if (readableByteChannel instanceof FileChannel && (contentSize > 1024 * 1024 || FAKE_MAPPING_FAIL)) {
            // It's quite expensive to map a file into the memory. Just do it when the box is larger than a MB.
            // AND on-demand
            this.contentSize = contentSize;
            this.startPosition = ((FileChannel) readableByteChannel).position();
            this.fileChannel = ((FileChannel) readableByteChannel);
            ((FileChannel) readableByteChannel).position(((FileChannel) readableByteChannel).position() + contentSize);
        } else {
            content = ChannelHelper.readFully(readableByteChannel, l2i(contentSize));
        }


    }

    public synchronized ByteBuffer getContent(long offset, int length) {
        if (content != null) {
            // The whole content is available in one bytebuffer! Everyting is fine.
            content.position(l2i(offset));
            ByteBuffer sample = content.slice();
            sample.limit(length);
            sample.rewind();
            return sample;
        } else {
            if (cache != null) {
                for (Map.Entry<Long, SoftReference<ByteBuffer>> entry : cache.entrySet()) {
                    if (entry.getKey() < offset) {
                        ByteBuffer cacheEntry = entry.getValue().get();
                        if ((cacheEntry != null) && ((entry.getKey() + cacheEntry.limit()) >= (offset + length))) {
                            // CACHE HIT
                            cacheEntry.position((int) (offset - entry.getKey()));
                            ByteBuffer cachedSample = cacheEntry.slice();
                            cachedSample.limit(length);
                            cachedSample.rewind();
                            return cachedSample;
                        }
                    }
                }
                // CACHE MISS
                ByteBuffer cacheEntry;
                try {
                    // Mapping whole file failed, mapping only 10MB at a time
                    cacheEntry = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPosition + offset, Math.min(10 * 1024 * 1024, contentSize - offset));
                } catch (IOException e1) {
                    LOG.fine("Even mapping just 10MB of the source file into the memory failed. " + e1);
                    throw new RuntimeException(
                            "Delayed reading of mdat content failed. Make sure not to close " +
                                    "the FileChannel that has been used to create the IsoFile!", e1);
                }
                cache.put(offset, new SoftReference<ByteBuffer>(cacheEntry));
                cacheEntry.position(0);
                ByteBuffer cachedSample = cacheEntry.slice();
                cachedSample.limit(length);
                cachedSample.rewind();
                return cachedSample;
            } else {
                try {
                    if (FAKE_MAPPING_FAIL) {
                        System.err.println("#############################");
                        System.err.println("### FAKING MEM MAP FAILED ###");
                        System.err.println("#############################");
                        throw new IOException("Intentional IOException to test coping with failed memory mapping");
                    }
                    content = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPosition, contentSize);
                    fileChannel = null;
                    LOG.fine("Successfully mapped the complete 'mdat' content into the memory.");
                    return getContent(offset, length);
                } catch (IOException e) {
                    LOG.fine("Mapping the complete 'mdat' content into the memory failed. Trying piece by piece from now on.");
                    cache = new HashMap<Long, SoftReference<ByteBuffer>>();
                    // Creating the cache makes sure no further memory mapping is tried
                    return getContent(offset, length);
                }
            }
        }
    }

    public ByteBuffer getHeader() {
        return header;
    }

}
