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

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class SampleAuxiliaryInformationSizesBox extends AbstractFullBox {
    public static final String TYPE = "saiz";

    private short defaultSampleInfoSize;
    private long sampleCount;
    private List<Short> sampleInfoSizes = new LinkedList<Short>();
    private long auxInfoType;
    private long auxInfoTypeParameter;

    public SampleAuxiliaryInformationSizesBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 9 + ((getFlags() & 1) == 1 ? 8 : 0) + (defaultSampleInfoSize == 0 ? sampleCount : 0);
    }

    @Override
    protected void getContent(ByteBuffer os) throws IOException {
        if ((getFlags() & 1) == 1) {
            IsoTypeWriter.writeUInt32(os, auxInfoType);
            IsoTypeWriter.writeUInt32(os, auxInfoTypeParameter);
        }

        IsoTypeWriter.writeUInt8(os, defaultSampleInfoSize);
        IsoTypeWriter.writeUInt32(os, sampleCount);

        for (short sampleInfoSize : sampleInfoSizes) {
            IsoTypeWriter.writeUInt8(os, sampleInfoSize);
        }
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if ((getFlags() & 1) == 1) {
            auxInfoType = IsoTypeReader.readUInt32(content);
            auxInfoTypeParameter = IsoTypeReader.readUInt32(content);
        }

        defaultSampleInfoSize = (short) IsoTypeReader.readUInt8(content);
        sampleCount = IsoTypeReader.readUInt32(content);

        sampleInfoSizes.clear();

        if (defaultSampleInfoSize == 0) {
            for (int i = 0; i < sampleCount; i++) {
                sampleInfoSizes.add((short) IsoTypeReader.readUInt8(content));
            }
        }
    }

    public long getAuxInfoType() {
        return auxInfoType;
    }

    public void setAuxInfoType(long auxInfoType) {
        this.auxInfoType = auxInfoType;
    }

    public long getAuxInfoTypeParameter() {
        return auxInfoTypeParameter;
    }

    public void setAuxInfoTypeParameter(long auxInfoTypeParameter) {
        this.auxInfoTypeParameter = auxInfoTypeParameter;
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
