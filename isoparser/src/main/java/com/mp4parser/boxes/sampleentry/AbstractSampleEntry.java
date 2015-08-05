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

package com.mp4parser.boxes.sampleentry;

import com.mp4parser.BoxParser;
import com.mp4parser.support.AbstractContainerBox;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Abstract base class for all sample entries.
 *
 * @see com.mp4parser.boxes.sampleentry.AudioSampleEntry
 * @see com.mp4parser.boxes.sampleentry.VisualSampleEntry
 * @see com.mp4parser.boxes.sampleentry.TextSampleEntry
 */
public abstract class AbstractSampleEntry extends AbstractContainerBox implements SampleEntry {


    protected int dataReferenceIndex = 1;

    protected AbstractSampleEntry(String type) {
        super(type);
    }


    public int getDataReferenceIndex() {
        return dataReferenceIndex;
    }

    public void setDataReferenceIndex(int dataReferenceIndex) {
        this.dataReferenceIndex = dataReferenceIndex;
    }

    @Override
    public abstract void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException;

    @Override
    public abstract void getBox(WritableByteChannel writableByteChannel) throws IOException;
}
