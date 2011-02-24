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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Samples within the media data are grouped into chunks. Chunks can be of different sizes, and the
 * samples within a chunk can have different sizes. This table can be used to find the chunk that
 * contains a sample, its position, and the associated sample description. Defined in ISO/IEC 14496-12.
 */
public class SampleToChunkBox extends AbstractFullBox {
    List<Entry> entries = Collections.emptyList();

    public static final String TYPE = "stsc";

    public SampleToChunkBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getDisplayName() {
        return "Sample to Chunk Box";
    }

    protected long getContentSize() {
        return entries.size() * 12 + 4;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long entryCount = in.readUInt32();
        if (entryCount > Integer.MAX_VALUE) {
            throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE entries!");
        }
        entries = new ArrayList<Entry>((int) entryCount);

        for (int i = 0; i < entryCount; i++) {
            Entry e = new Entry();
            e.setFirstChunk(in.readUInt32());
            e.setSamplesPerChunk(in.readUInt32());
            e.setSampleDescriptionIndex(in.readUInt32());
            entries.add(e);
        }
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        long l = isos.getStreamPosition();
        isos.writeUInt32(entries.size());
        for (Entry entry : entries) {
            isos.writeUInt32(entry.getFirstChunk());
            isos.writeUInt32(entry.getSamplesPerChunk());
            isos.writeUInt32(entry.getSampleDescriptionIndex());

        }
        assert getContentSize() == (isos.getStreamPosition() - l);
    }

    public String toString() {
        return "SampleToChunkBox[entryCount=" + entries.size() + "]";
    }

    public static class Entry {
        long firstChunk;
        long samplesPerChunk;
        long sampleDescriptionIndex;

        public long getFirstChunk() {
            return firstChunk;
        }

        public void setFirstChunk(long firstChunk) {
            this.firstChunk = firstChunk;
        }

        public long getSamplesPerChunk() {
            return samplesPerChunk;
        }

        public void setSamplesPerChunk(long samplesPerChunk) {
            this.samplesPerChunk = samplesPerChunk;
        }

        public long getSampleDescriptionIndex() {
            return sampleDescriptionIndex;
        }

        public void setSampleDescriptionIndex(long sampleDescriptionIndex) {
            this.sampleDescriptionIndex = sampleDescriptionIndex;
        }
    }
}
