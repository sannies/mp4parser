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

package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

public class AppleDataRateBox extends AbstractFullBox {
    public static final String TYPE = "rmdr";
    private long dataRate;

    public AppleDataRateBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return 4;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        dataRate = in.readUInt32();
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(dataRate);
    }

    public long getDataRate() {
        return dataRate;
    }
}
