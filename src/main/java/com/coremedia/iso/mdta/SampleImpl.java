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

package com.coremedia.iso.mdta;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.TrackMetaDataContainer;

import java.io.IOException;
import java.util.List;

/**
 * An object of this class represents a single sample of an IsoFile as in the file read.
 * The sample has a {@link Chunk} as parent.
 *
 * @see Chunk
 * @see Track
 * @see Sample
 */
public class SampleImpl<T extends TrackMetaDataContainer> implements Sample<T>, Comparable<SampleImpl<T>> {

    private final Chunk<T> parent;
    protected final IsoBufferWrapper buffer;
    protected final long offset;
    protected final long size;
    protected boolean syncSample;
    private long sampleNumber;

    public SampleImpl(IsoBufferWrapper buffer, long offset, Long sampleNumber, long size, Chunk<T> parent, boolean syncSample) {
        this.parent = parent;
        this.buffer = buffer;
        this.offset = offset;
        this.size = size;
        this.syncSample = syncSample;
        this.sampleNumber = sampleNumber;
    }

    public void getContent(IsoOutputStream os) throws IOException {
        //System.out.println("SampleImpl#getContent " + this.getSampleNumber());
        IsoBufferWrapper isoBufferWrapper = buffer.getSegment(offset, size);
        while (isoBufferWrapper.remaining() > 1024) {
            byte[] buf = new byte[1024];
            isoBufferWrapper.read(buf);
            os.write(buf);
        }
        while (isoBufferWrapper.remaining() > 0) {
            os.write(isoBufferWrapper.read());
        }
    }


    public long getSize() {
        return size;
    }

    public long getOffset() {
        return offset;
    }

    public String toString() {
        //System.out.println("SampleImpl#toString");
        return "Offset: " + calculateOffset() + " Size: " + size + " Chunk: " + parent.getFirstSample().calculateOffset() + " Track: " + parent.getParentTrack().getTrackId() + " SyncSample: " + syncSample;
    }

    public int compareTo(SampleImpl<T> o) {
        return (int) (this.offset - o.offset);
    }

    public Chunk<T> getParent() {
        return parent;
    }

    public long calculateOffset() {
        long offsetFromChunkStart = 0;
        List<Sample<T>> samples = parent.getSamples();
        for (Sample<T> sample : samples) {
            if (!this.equals(sample)) {
                offsetFromChunkStart += sample.getSize();
            }
        }
        return parent.calculateOffset() + offsetFromChunkStart;
    }

    public boolean isSyncSample() {
        return syncSample;
    }

    public String getDescription() {
        return null;
    }

    public long getSampleNumber() {
        return sampleNumber;
    }
}
