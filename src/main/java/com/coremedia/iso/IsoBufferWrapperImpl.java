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

package com.coremedia.iso;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>FilterInputStream</code> enriched with helper methods to ease writing of
 * Iso specific numbers and strings.
 */
public class IsoBufferWrapperImpl extends AbstractIsoBufferWrapper {
    ByteBuffer[] parents;
    int activeParent = 0;


    public IsoBufferWrapperImpl(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public IsoBufferWrapperImpl(ByteBuffer parent) {
        this.parents = new ByteBuffer[]{parent};
    }

    public IsoBufferWrapperImpl(ByteBuffer[] parents) {
        this.parents = parents;
    }

    public IsoBufferWrapperImpl(List<ByteBuffer> parents) {
        this.parents = parents.toArray(new ByteBuffer[parents.size()]);
    }

    public IsoBufferWrapperImpl(File file) throws IOException {
        long filelength = file.length();
        int sliceSize = 1024 * 1024 * 128;

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
        long i = 0;
        while (i < filelength) {
            if ((filelength - i) > sliceSize) {
                ByteBuffer bb;
                try {
                    bb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, i, sliceSize);//.slice();
                } catch (IOException e1) {

                    try {
                        bb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, i, sliceSize);//.slice();
                    } catch (IOException e2) {

                        try {
                            bb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, i, sliceSize);//.slice();
                        } catch (IOException e3) {
                            bb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, i, sliceSize);//.slice();
                        }
                    }
                }
                buffers.add(bb);
                i += sliceSize;
            } else {
                buffers.add(raf.getChannel().map(FileChannel.MapMode.READ_ONLY, i, filelength - i).slice());
                i += filelength - i;
            }
        }
        parents = buffers.toArray(new ByteBuffer[buffers.size()]);
        raf.close();
    }

    public long position() {
        if (activeParent >= 0) {
            long pos = 0;
            for (int i = 0; i < activeParent; i++) {
                pos += parents[i].limit();
            }
            pos += parents[activeParent].position();
            return pos;
        } else {
            return size();
        }
    }

    public void position(long position) {
        if (position == size()) {
            activeParent = -1;
        } else {
            int current = 0;
            while (position >= parents[current].limit()) {
                position -= parents[current++].limit();
            }
            parents[current].position((int) position);
            activeParent = current;
        }
    }

    public long size() {
        long size = 0;
        for (ByteBuffer parent : parents) {
            size += parent.limit();
        }
        return size;
    }


    public long remaining() {
        if (activeParent == -1) {
            return 0;
        } else {
            long remaining = 0;
            for (int i = activeParent; i < parents.length; i++) {
                remaining += parents[i].remaining();

            }
            return remaining;
        }
    }


    public int read() {
        if (parents[activeParent].remaining() == 0) {
            if (parents.length > activeParent + 1) {
                activeParent++;
                parents[activeParent].rewind();
                return read();
            } else {
                return -1;
            }
        }
        int b = parents[activeParent].get();
        return b < 0 ? b + 256 : b;
    }

    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) {
        if (parents[activeParent].remaining() >= len) {
            parents[activeParent].get(b, off, len);
            return len;
        } else {
            int curRemaining = parents[activeParent].remaining();
            parents[activeParent].get(b, off, curRemaining);
            activeParent++;
            parents[activeParent].rewind();
            return curRemaining + read(b, off + curRemaining, len - curRemaining);
        }

    }


    public IsoBufferWrapper getSegment(long startPos, long length) {
        long savePos = this.position();
        ArrayList<ByteBuffer> segments = new ArrayList<ByteBuffer>();
        position(startPos);
        while (length > 0) {
            ByteBuffer currentSlice = parents[activeParent].slice();
            if (currentSlice.remaining() >= length) {
                currentSlice.limit((int) length); // thats ok we tested in the line before
                length -= length;
            } else {
                // ok use up current bytebuffer and jump to next
                length -= currentSlice.remaining();
                parents[++activeParent].rewind();
            }
            segments.add(currentSlice);
        }
        position(savePos);
        return new IsoBufferWrapperImpl(segments.toArray(new ByteBuffer[segments.size()]));
    }


}
