/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * aligned(8) class TrackExtendsBox extends FullBox('trex', 0, 0){
 * unsigned int(32) track_ID;
 * unsigned int(32) default_sample_description_index;
 * unsigned int(32) default_sample_duration;
 * unsigned int(32) default_sample_size;
 * unsigned int(32) default_sample_flags
 * }
 */
public class TrackExtendsBox extends AbstractFullBox {
    public static final String TYPE = "trex";
    private long trackId;
    private long defaultSampleDescriptionIndex;
    private long defaultSampleDuration;
    private long defaultSampleSize;
    private SampleFlags defaultSampleFlags;

    public TrackExtendsBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getDisplayName() {
        return "Track Extends Box";
    }

    @Override
    protected long getContentSize() {
        return 5 * 4;
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(trackId);
        os.writeUInt32(defaultSampleDescriptionIndex);
        os.writeUInt32(defaultSampleDuration);
        os.writeUInt32(defaultSampleSize);
        defaultSampleFlags.getContent(os);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);

        trackId = in.readUInt32();
        defaultSampleDescriptionIndex = in.readUInt32();
        defaultSampleDuration = in.readUInt32();
        defaultSampleSize = in.readUInt32();
        defaultSampleFlags = new SampleFlags(in.readUInt32());
    }

    public long getTrackId() {
        return trackId;
    }

    public long getDefaultSampleDescriptionIndex() {
        return defaultSampleDescriptionIndex;
    }

    public long getDefaultSampleDuration() {
        return defaultSampleDuration;
    }

    public long getDefaultSampleSize() {
        return defaultSampleSize;
    }

    public String getDefaultSampleFlags() {
        return defaultSampleFlags.toString();
    }
}
