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
 * <code>class BitRateBox extends Box('btrt') {<br/>
 * unsigned int(32) bufferSizeDB;<br/>
 * // gives the size of the decoding buffer for<br/>
 * // the elementary stream in bytes.<br/>
 * unsigned int(32) maxBitrate;<br/>
 * // gives the maximum rate in bits/second <br/>
 * // over any window of one second.<br/>
 * unsigned int(32) avgBitrate;<br/>
 * // avgBitrate gives the average rate in <br/>
 * // bits/second over the entire presentation.<br/>
 * }</code>
 */

public final class BitRateBox extends AbstractBox {
    public static final String TYPE = "btrt";

    private long bufferSizeDb;
    private long maxBitrate;
    private long avgBitrate;

    public BitRateBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return 12;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        bufferSizeDb = in.readUInt32();
        maxBitrate = in.readUInt32();
        avgBitrate = in.readUInt32();
    }

    public String getDisplayName() {
        return "Bit Rate Box";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(bufferSizeDb);
        os.writeUInt32(maxBitrate);
        os.writeUInt32(avgBitrate);
    }

    public long getBufferSizeDb() {
        return bufferSizeDb;
    }

    public void setBufferSizeDb(long bufferSizeDb) {
        this.bufferSizeDb = bufferSizeDb;
    }

    public long getMaxBitrate() {
        return maxBitrate;
    }

    public void setMaxBitrate(long maxBitrate) {
        this.maxBitrate = maxBitrate;
    }

    public long getAvgBitrate() {
        return avgBitrate;
    }

    public void setAvgBitrate(long avgBitrate) {
        this.avgBitrate = avgBitrate;
    }
}
