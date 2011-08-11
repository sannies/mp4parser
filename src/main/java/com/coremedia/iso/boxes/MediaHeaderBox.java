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
 * This box defines overall information which is media-independent, and relevant to the entire presentation
 * considered as a whole.
 */
public class MediaHeaderBox extends AbstractFullBox {
    public static final String TYPE = "mdhd";


    private long creationTime;
    private long modificationTime;
    private long timescale;
    private long duration;
    private String language;

    public MediaHeaderBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public long getTimescale() {
        return timescale;
    }

    public long getDuration() {
        return duration;
    }

    public String getLanguage() {
        return language;
    }

    protected long getContentSize() {
        long contentSize = 0;
        if (getVersion() == 1) {
            contentSize += 8 + 8 + 4 + 8;
        } else {
            contentSize += 4 + 4 + 4 + 4;
        }
        contentSize += 2;
        contentSize += 2;
        return contentSize;

    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setTimescale(long timescale) {
        this.timescale = timescale;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        if (getVersion() == 1) {
            creationTime = in.readUInt64();
            modificationTime = in.readUInt64();
            timescale = in.readUInt32();
            duration = in.readUInt64();
        } else {
            creationTime = in.readUInt32();
            modificationTime = in.readUInt32();
            timescale = in.readUInt32();
            duration = in.readUInt32();
        }
        language = in.readIso639();
        in.readUInt16();
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("MovieHeaderBox[");
        result.append("creationTime=").append(getCreationTime());
        result.append(";");
        result.append("modificationTime=").append(getModificationTime());
        result.append(";");
        result.append("timescale=").append(getTimescale());
        result.append(";");
        result.append("duration=").append(getDuration());
        result.append(";");
        result.append("language=").append(getLanguage());
        result.append("]");
        return result.toString();
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        if (getVersion() == 1) {
            isos.writeUInt64(creationTime);
            isos.writeUInt64(modificationTime);
            isos.writeUInt32(timescale);
            isos.writeUInt64(duration);
        } else {
            isos.writeUInt32((int) creationTime);
            isos.writeUInt32((int) modificationTime);
            isos.writeUInt32((int) timescale);
            isos.writeUInt32((int) duration);
        }
        isos.writeIso639(language);
        isos.writeUInt16(0);
    }
}
