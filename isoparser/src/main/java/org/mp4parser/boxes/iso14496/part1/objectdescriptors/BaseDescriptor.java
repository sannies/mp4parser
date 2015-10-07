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

import java.io.IOException;
import java.nio.ByteBuffer;

/*
abstract aligned(8) expandable(228-1) class BaseDescriptor : bit(8) tag=0 {
// empty. To be filled by classes extending this class.
}

int sizeOfInstance = 0;
bit(1) nextByte;
bit(7) sizeOfInstance;
while(nextByte) {
bit(1) nextByte;
bit(7) sizeByte;
sizeOfInstance = sizeOfInstance<<7 | sizeByte;
}
 */
@Descriptor(tags = 0x00)
public abstract class BaseDescriptor {
    int tag;
    int sizeOfInstance;
    int sizeBytes;

    public BaseDescriptor() {
    }

    public int getTag() {
        return tag;
    }

    public void writeSize(ByteBuffer bb, int size) {
        int pos = bb.position();

        int i = 0;
        while (size > 0 || i < sizeBytes) {
            i++;
            if (size > 0) {
                bb.put(pos + getSizeSize() - i, (byte) (size & 0x7f));
            } else {
                bb.put(pos + getSizeSize() - i, (byte) (0x80));
            }
            size >>>= 7;

        }

        bb.position(pos + getSizeSize());

    }

    public int getSizeSize() {
        int size = getContentSize();
        int i = 0;
        while (size > 0 || i < sizeBytes) {
            size >>>= 7;
            i++;
        }
        return i;
    }


    public int getSize() {
        return getContentSize() + getSizeSize() + 1;
    }

    public final void parse(int tag, ByteBuffer bb) throws IOException {
        this.tag = tag;

        int i = 0;
        int tmp = IsoTypeReader.readUInt8(bb);
        i++;
        sizeOfInstance = tmp & 0x7f;
        while (tmp >>> 7 == 1) { //nextbyte indicator bit
            tmp = IsoTypeReader.readUInt8(bb);
            i++;
            //sizeOfInstance = sizeOfInstance<<7 | sizeByte;
            sizeOfInstance = sizeOfInstance << 7 | tmp & 0x7f;
        }
        sizeBytes = i;
        ByteBuffer detailSource = bb.slice();
        detailSource.limit(sizeOfInstance);
        parseDetail(detailSource);
        assert detailSource.remaining() == 0 : this.getClass().getSimpleName() + " has not been fully parsed";
        bb.position(bb.position() + sizeOfInstance);
    }

    public abstract void parseDetail(ByteBuffer bb) throws IOException;

    public abstract ByteBuffer serialize();

    abstract int getContentSize();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BaseDescriptor");
        sb.append("{tag=").append(tag);
        sb.append(", sizeOfInstance=").append(sizeOfInstance);
        sb.append('}');
        return sb.toString();
    }
}
