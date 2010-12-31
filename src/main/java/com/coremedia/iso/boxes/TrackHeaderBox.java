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
import com.coremedia.iso.assistui.isDate;

import java.io.IOException;

/**
 * This box specifies the characteristics of a single track. Exactly one Track Header Box is contained in a track.<br>
 * In the absence of an edit list, the presentation of a track starts at the beginning of the overall presentation. An
 * empty edit is used to offset the start time of a track. <br>
 * The default value of the track header flags for media tracks is 7 (track_enabled, track_in_movie,
 * track_in_preview). If in a presentation all tracks have neither track_in_movie nor track_in_preview set, then all
 * tracks shall be treated as if both flags were set on all tracks. Hint tracks should have the track header flags set
 * to 0, so that they are ignored for local playback and preview.
 */
public class TrackHeaderBox extends AbstractFullBox {

    public static final String TYPE = "tkhd";

    private long creationTime;
    private long modificationTime;
    private long trackId;
    private long duration;
    private int layer;
    private int alternateGroup;
    private float volume;
    private long[] matrix;
    private double width;
    private double height;

    public TrackHeaderBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @isDate
    public long getCreationTime() {
        return creationTime;
    }

    @isDate
    public long getModificationTime() {
        return modificationTime;
    }

    public long getTrackId() {
        return trackId;
    }

    public long getDuration() {
        return duration;
    }

    public int getLayer() {
        return layer;
    }

    public int getAlternateGroup() {
        return alternateGroup;
    }

    public float getVolume() {
        return volume;
    }

    public long[] getMatrix() {
        return matrix;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public String getDisplayName() {
        return "Track Header Box";
    }

    protected long getContentSize() {
        long contentSize = 0;
        if (getVersion() == 1) {
            contentSize += 32;
        } else {
            contentSize += 20;
        }
        contentSize += 60;
        return contentSize;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox); //172
        if (getVersion() == 1) {
            creationTime = in.readUInt64();
            modificationTime = in.readUInt64();
            trackId = in.readUInt32();
            in.readUInt32();
            duration = in.readUInt64();
        } else {
            creationTime = in.readUInt32();
            modificationTime = in.readUInt32();
            trackId = in.readUInt32();
            in.readUInt32();
            duration = in.readUInt32();
        } // 196
        in.readUInt32();
        in.readUInt32();
        layer = in.readUInt16();    // 204
        alternateGroup = in.readUInt16();
        volume = in.readFixedPoint88();
        in.readUInt16();     // 212
        matrix = new long[9];
        for (int i = 0; i < 9; i++) {
            matrix[i] = in.readUInt32();
        }
        width = in.readFixedPoint1616();    // 248
        height = in.readFixedPoint1616();
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        if (getVersion() == 1) {
            isos.writeUInt64(creationTime);
            isos.writeUInt64(modificationTime);
            isos.writeUInt32(trackId);
            isos.writeUInt32(0);
            isos.writeUInt64(duration);
        } else {
            isos.writeUInt32((int) creationTime);
            isos.writeUInt32((int) modificationTime);
            isos.writeUInt32(trackId);
            isos.writeUInt32(0);
            isos.writeUInt32((int) duration);
        } // 196
        isos.writeUInt32(0);
        isos.writeUInt32(0);
        isos.writeUInt16(layer);
        isos.writeUInt16(alternateGroup);
        isos.writeFixedPont88(volume);
        isos.writeUInt16(0);
        for (int i = 0; i < 9; i++) {
            isos.writeUInt32(matrix[i]);
        }
        isos.writeFixedPont1616(width);
        isos.writeFixedPont1616(height);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("TrackHeaderBox[");
        result.append("creationTime=").append(getCreationTime());
        result.append(";");
        result.append("modificationTime=").append(getModificationTime());
        result.append(";");
        result.append("trackId=").append(getTrackId());
        result.append(";");
        result.append("duration=").append(getDuration());
        result.append(";");
        result.append("layer=").append(getLayer());
        result.append(";");
        result.append("alternateGroup=").append(getAlternateGroup());
        result.append(";");
        result.append("volume=").append(getVolume());
        for (int i = 0; i < matrix.length; i++) {
            result.append(";");
            result.append("matrix").append(i).append("=").append(matrix[i]);
        }
        result.append(";");
        result.append("width=").append(getWidth());
        result.append(";");
        result.append("height=").append(getHeight());
        result.append("]");
        return result.toString();
    }
}
