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

package com.mp4parser.boxes.apple;

import com.mp4parser.IsoFile;
import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.tools.Utf8;
import com.mp4parser.support.AbstractFullBox;

import java.nio.ByteBuffer;

import static com.mp4parser.tools.CastUtils.l2i;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class AppleDataReferenceBox extends AbstractFullBox {
    public static final String TYPE = "rdrf";
    private int dataReferenceSize;
    private String dataReferenceType;
    private String dataReference;

    public AppleDataReferenceBox() {
        super(TYPE);
    }


    protected long getContentSize() {
        return 12 + dataReferenceSize;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        dataReferenceType = IsoTypeReader.read4cc(content);
        dataReferenceSize = l2i(IsoTypeReader.readUInt32(content));
        dataReference = IsoTypeReader.readString(content, dataReferenceSize);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(IsoFile.fourCCtoBytes(dataReferenceType));
        IsoTypeWriter.writeUInt32(byteBuffer, dataReferenceSize);
        byteBuffer.put(Utf8.convert(dataReference));
    }

    public long getDataReferenceSize() {
        return dataReferenceSize;
    }

    public String getDataReferenceType() {
        return dataReferenceType;
    }

    public String getDataReference() {
        return dataReference;
    }
}
