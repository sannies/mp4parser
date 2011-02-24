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

import java.io.IOException;

/**
 * The chunk offset table gives the index of each chunk into the containing file. Defined in ISO/IEC 14496-12.
 */
public class StaticChunkOffsetBox extends ChunkOffsetBox {
    public static final String TYPE = "stco";

    private long[] chunkOffsets = new long[0];

    public long[] getChunkOffsets() {
        return chunkOffsets;
    }

    protected long getContentSize() {
        return getChunkOffsets().length * 4 + 4;
    }

    public void setChunkOffsets(long[] chunkOffsets) {
        this.chunkOffsets = chunkOffsets;
    }


    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long entryCount = in.readUInt32();
        if (entryCount > Integer.MAX_VALUE) {
            throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE entries!");
        }
        chunkOffsets = new long[(int) entryCount];
        for (int i = 0; i < entryCount; i++) {
            chunkOffsets[i] = in.readUInt32();
        }
    }
}
