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

public class TrackFragmentBaseMediaDecodeTimeBox extends AbstractFullBox {
    public static final String TYPE = "tfdt";

    private long baseMediaDecodeTime;

    public TrackFragmentBaseMediaDecodeTimeBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    protected long getContentSize() {
        return getVersion() == 0 ? 4 : 8;
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        if (getVersion() == 1) {
            os.writeUInt64(baseMediaDecodeTime);
        } else {
            os.writeUInt32(baseMediaDecodeTime);
        }
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        if (getVersion() == 1) {
            baseMediaDecodeTime = in.readUInt64();
        } else {
            baseMediaDecodeTime = in.readUInt32();
        }
    }

    public long getBaseMediaDecodeTime() {
        return baseMediaDecodeTime;
    }

    public void setBaseMediaDecodeTime(long baseMediaDecodeTime) {
        this.baseMediaDecodeTime = baseMediaDecodeTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TrackFragmentBaseMediaDecodeTimeBox");
        sb.append("{baseMediaDecodeTime=").append(baseMediaDecodeTime);
        sb.append('}');
        return sb.toString();
    }
}
