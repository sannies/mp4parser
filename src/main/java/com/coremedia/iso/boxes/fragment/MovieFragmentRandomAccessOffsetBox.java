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
import com.coremedia.iso.boxes.BoxInterface;

import java.io.IOException;

/**
 * aligned(8) class MovieFragmentRandomAccessOffsetBox
 * extends FullBox('mfro', version, 0) {
 * unsigned int(32) size;
 * }
 */
public class MovieFragmentRandomAccessOffsetBox extends AbstractFullBox {
    public static final String TYPE = "mfro";
    private long mfraSize;

    public MovieFragmentRandomAccessOffsetBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getDisplayName() {
        return "Movie Fragment Random Access Offset Box";
    }

    protected long getContentSize() {
        return 4;
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(mfraSize);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);

        mfraSize = in.readUInt32();
    }

    public long getMfraSize() {
        return mfraSize;
    }
}
