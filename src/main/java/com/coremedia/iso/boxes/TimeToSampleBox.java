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

/**
 * This box contains a compact version of a table that allows indexing from decoding time to sample number.
 * Other tables give sample sizes and pointers, from the sample number. Each entry in the table gives the
 * number of consecutive samples with the same time delta, and the delta of those samples. By adding the
 * deltas a complete time-to-sample map may be built.<br>
 * The Decoding Time to Sample Box contains decode time delta's: <code>DT(n+1) = DT(n) + STTS(n)</code> where STTS(n)
 * is the (uncompressed) table entry for sample n.<br>
 * The sample entries are ordered by decoding time stamps; therefore the deltas are all non-negative. <br>
 * The DT axis has a zero origin; <code>DT(i) = SUM(for j=0 to i-1 of delta(j))</code>, and the sum of all
 * deltas gives the length of the media in the track (not mapped to the overall timescale, and not considering
 * any edit list).    <br>
 * The Edit List Box provides the initial CT value if it is non-empty (non-zero).
 */
public class TimeToSampleBox extends AbstractFullBox {
    private long[] sampleCount;
    private long[] sampleDelta;
    public static final String TYPE = "stts";

    public TimeToSampleBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public long[] getSampleCount() {
        return sampleCount;
    }

    public long[] getSampleDelta() {
        return sampleDelta;
    }

    public String getDisplayName() {
        return "Decoding Time to Sample Box";
    }

    protected long getContentSize() {
        return 4 +
                sampleCount.length * 4 +
                sampleDelta.length * 4;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long entryCount = in.readUInt32();
        if (entryCount > Integer.MAX_VALUE) {
            throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE entries!");
        }
        sampleCount = new long[(int) entryCount];
        sampleDelta = new long[(int) entryCount];
        for (int i = 0; i < entryCount; i++) {
            sampleCount[i] = in.readUInt32();
            sampleDelta[i] = in.readUInt32();
        }
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt32(sampleCount.length);
        for (int i = 0; i < sampleCount.length; i++) {
            isos.writeUInt32(sampleCount[i]);
            isos.writeUInt32(sampleDelta[i]);

        }
    }

    public String toString() {
        return "TimeToSampleBox[entryCount=" + sampleCount.length + "]";
    }
}
