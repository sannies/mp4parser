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

package org.mp4parser.boxes.iso14496.part1.objectdescriptors;

import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * class ProfileLevelIndicationIndexDescriptor () extends BaseDescriptor
 * : bit(8) ProfileLevelIndicationIndexDescrTag {
 * bit(8) profileLevelIndicationIndex;
 * }
 */
@Descriptor(tags = 0x14)
public class ProfileLevelIndicationDescriptor extends BaseDescriptor {
    int profileLevelIndicationIndex;

    public ProfileLevelIndicationDescriptor() {
        tag = 0x14;
    }

    @Override
    public void parseDetail(ByteBuffer bb) throws IOException {
        profileLevelIndicationIndex = IsoTypeReader.readUInt8(bb);
    }

    @Override
    public ByteBuffer serialize() {
        ByteBuffer out = ByteBuffer.allocate(getSize());
        IsoTypeWriter.writeUInt8(out, 0x14);
        writeSize(out, getContentSize());
        IsoTypeWriter.writeUInt8(out, profileLevelIndicationIndex);
        return out;
    }

    @Override
    public int getContentSize() {
        return 1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ProfileLevelIndicationDescriptor");
        sb.append("{profileLevelIndicationIndex=").append(Integer.toHexString(profileLevelIndicationIndex));
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

        ProfileLevelIndicationDescriptor that = (ProfileLevelIndicationDescriptor) o;

        if (profileLevelIndicationIndex != that.profileLevelIndicationIndex) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return profileLevelIndicationIndex;
    }
}
