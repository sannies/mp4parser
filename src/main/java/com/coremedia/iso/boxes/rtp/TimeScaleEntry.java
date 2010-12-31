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

package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxInterface;

import java.io.IOException;

/**
 * The TimeScaleEntry is a required box in the additionalData array of a HintSampleEntry if
 * the HintSampleEntry's type is 'rtp '.
 *
 * @see HintSampleEntry
 */
public class TimeScaleEntry extends Box {
    private long timescale;
    public static final String TYPE = "tims";

    public TimeScaleEntry() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public long getTimescale() {
        return timescale;
    }

    public String getDisplayName() {
        return "Time Scale Entry";
    }

    protected long getContentSize() {
        return 4;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        assert size == 4;
        timescale = in.readUInt32();
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(timescale);
    }

    public String toString() {
        return "TimeScaleEntry[timescale=" + getTimescale() + "]";
    }
}
