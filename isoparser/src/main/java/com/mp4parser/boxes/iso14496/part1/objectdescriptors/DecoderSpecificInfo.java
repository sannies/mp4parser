/*
 * Copyright 2011 castLabs, Berlin
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

package com.mp4parser.boxes.iso14496.part1.objectdescriptors;

import com.mp4parser.tools.Hex;
import com.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * abstract class DecoderSpecificInfo extends BaseDescriptor : bit(8)
 * tag=DecSpecificInfoTag
 * {
 * // empty. To be filled by classes extending this class.
 * }
 */
@Descriptor(tags = 0x05)
public class DecoderSpecificInfo extends BaseDescriptor {
    byte[] bytes;

    public DecoderSpecificInfo() {
        tag = 0x5;
    }

    @Override
    public void parseDetail(ByteBuffer bb) throws IOException {
        bytes = new byte[bb.remaining()];
        bb.get(bytes);
    }

    public void setData(byte[] bytes) {
        this.bytes = bytes;
    }

    int getContentSize() {
        return bytes.length;
    }

    public ByteBuffer serialize() {
        ByteBuffer out = ByteBuffer.allocate(getSize());
        IsoTypeWriter.writeUInt8(out, tag);
        writeSize(out, getContentSize());
        out.put(bytes);
        return (ByteBuffer) out.rewind();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DecoderSpecificInfo");
        sb.append("{bytes=").append(bytes == null ? "null" : Hex.encodeHex(bytes));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DecoderSpecificInfo that = (DecoderSpecificInfo) o;

        if (!Arrays.equals(bytes, that.bytes)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return bytes != null ? Arrays.hashCode(bytes) : 0;
    }
}
