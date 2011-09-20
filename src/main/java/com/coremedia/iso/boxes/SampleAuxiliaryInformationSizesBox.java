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

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SampleAuxiliaryInformationSizesBox extends AbstractFullBox {
    public static final String TYPE = "saiz";

    private short defaultSampleInfoSize;
    private long sampleCount;
    private List<Short> sampleInfoSizes = new LinkedList<Short>();

    public SampleAuxiliaryInformationSizesBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    protected long getContentSize() {
        return 5 + defaultSampleInfoSize == 0 ? sampleCount : 0;
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt8(defaultSampleInfoSize);
        os.writeUInt32(sampleCount);

        for (short sampleInfoSize : sampleInfoSizes) {
            os.writeUInt8(sampleInfoSize);
        }
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        defaultSampleInfoSize = (short) in.readUInt8();
        sampleCount = in.readUInt32();

        sampleInfoSizes.clear();

        if (defaultSampleInfoSize == 0) {
            for (int i = 0; i < sampleCount; i++) {
                sampleInfoSizes.add((short) in.readUInt8());
            }
        }
    }

    public short getDefaultSampleInfoSize() {
        return defaultSampleInfoSize;
    }

    public void setDefaultSampleInfoSize(short defaultSampleInfoSize) {
        this.defaultSampleInfoSize = defaultSampleInfoSize;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public List<Short> getSampleInfoSizes() {
        return sampleInfoSizes;
    }

    public void setSampleInfoSizes(List<Short> sampleInfoSizes) {
        this.sampleInfoSizes = sampleInfoSizes;
    }
}
