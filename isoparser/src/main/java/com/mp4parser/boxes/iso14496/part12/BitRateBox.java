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

package com.mp4parser.boxes.iso14496.part12;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.support.AbstractBox;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * class BitRateBox extends Box('btrt') {
 *  unsigned int(32) bufferSizeDB;
 *  // gives the size of the decoding buffer for
 *  // the elementary stream in bytes.
 *  unsigned int(32) maxBitrate;
 *  // gives the maximum rate in bits/second
 *  // over any window of one second.
 *  unsigned int(32) avgBitrate;
 *  // avgBitrate gives the average rate in
 *  // bits/second over the entire presentation.
 * }</pre>
 */

public final class BitRateBox extends AbstractBox {
    public static final String TYPE = "btrt";

    private long bufferSizeDb;
    private long maxBitrate;
    private long avgBitrate;

    public BitRateBox() {
        super(TYPE);
    }

    protected long getContentSize() {
        return 12;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        bufferSizeDb = IsoTypeReader.readUInt32(content);
        maxBitrate = IsoTypeReader.readUInt32(content);
        avgBitrate = IsoTypeReader.readUInt32(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeUInt32(byteBuffer, bufferSizeDb);
        IsoTypeWriter.writeUInt32(byteBuffer, maxBitrate);
        IsoTypeWriter.writeUInt32(byteBuffer, avgBitrate);
    }

    /**
     * Get the size of the decoding buffer for the elementary stream in bytes.
     * @return decoding buffer size
     */
    public long getBufferSizeDb() {
        return bufferSizeDb;
    }

    /**
     * Sets the size of the decoding buffer for the elementary stream in bytes
     * @param bufferSizeDb decoding buffer size
     */
    public void setBufferSizeDb(long bufferSizeDb) {
        this.bufferSizeDb = bufferSizeDb;
    }

    /**
     * gets the maximum rate in bits/second over any window of one second.
     * @return max bit rate
     */
    public long getMaxBitrate() {
        return maxBitrate;
    }

    /**
     * Sets the maximum rate in bits/second over any window of one second.
     * @param maxBitrate max bit rate
     */
    public void setMaxBitrate(long maxBitrate) {
        this.maxBitrate = maxBitrate;
    }

    /**
     * Gets the average rate in bits/second over the entire presentation.
     * @return average bit rate
     */
    public long getAvgBitrate() {
        return avgBitrate;
    }

    /**
     * Sets the average rate in bits/second over the entire presentation.
     * @param avgBitrate the track's average bit rate
     */
    public void setAvgBitrate(long avgBitrate) {
        this.avgBitrate = avgBitrate;
    }
}
