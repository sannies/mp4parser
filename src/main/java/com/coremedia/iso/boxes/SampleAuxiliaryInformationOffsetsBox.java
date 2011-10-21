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

/*
aligned(8) class SampleAuxiliaryInformationOffsetsBox
            extends FullBox(‘saio’, version, flags)
{
            if (flags & 1) {
                        unsigned int(32) aux_info_type;
                        unsigned int(32) aux_info_type_parameter;
            }
            unsigned int(32) entry_count;
            if ( version == 0 )
            {
                        unsigned int(32) offset[ entry_count ];
            }
            else
            {
                        unsigned int(64) offset[ entry_count ];
            }
}
 */
public class SampleAuxiliaryInformationOffsetsBox extends AbstractFullBox {
    public static final String TYPE = "saio";

    private long entryCount;
    private List<Long> offsets = new LinkedList<Long>();
    private long auxInfoType;
    private long auxInfoTypeParameter;

    public SampleAuxiliaryInformationOffsetsBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    protected long getContentSize() {
        return 4 + (getVersion() == 0 ? 4 * entryCount : 8 * entryCount) + ((getFlags() & 1) == 1 ? 8 : 0);
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        if ((getFlags() & 1) == 1) {
            os.writeUInt32(auxInfoType);
            os.writeUInt32(auxInfoTypeParameter);
        }

        os.writeUInt32(entryCount);
        for (Long offset : offsets) {
            if (getVersion() == 0) {
                os.writeUInt32(offset);
            } else {
                os.writeUInt64(offset);
            }
        }
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);

        if ((getFlags() & 1) == 1) {
            auxInfoType = in.readUInt32();
            auxInfoTypeParameter = in.readUInt32();
        }

        entryCount = in.readUInt32();
        offsets.clear();

        for (int i = 0; i < entryCount; i++) {
            if (getVersion() == 0) {
                offsets.add(in.readUInt32());
            } else {
                offsets.add(in.readUInt64());
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

    public long getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(long entryCount) {
        this.entryCount = entryCount;
    }

    public List<Long> getOffsets() {
        return offsets;
    }

    public void setOffsets(List<Long> offsets) {
        this.offsets = offsets;
    }
}
