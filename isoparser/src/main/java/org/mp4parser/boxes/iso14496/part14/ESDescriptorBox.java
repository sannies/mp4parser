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

package org.mp4parser.boxes.iso14496.part14;

import org.mp4parser.boxes.iso14496.part1.objectdescriptors.ESDescriptor;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * ES Descriptor Box.
 */
public class ESDescriptorBox extends AbstractDescriptorBox {
    public static final String TYPE = "esds";


    public ESDescriptorBox() {
        super(TYPE);
    }

    public ESDescriptor getEsDescriptor() {
        return (ESDescriptor) super.getDescriptor();
    }

    public void setEsDescriptor(ESDescriptor esDescriptor) {
        super.setDescriptor(esDescriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ESDescriptorBox that = (ESDescriptorBox) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    protected long getContentSize() {
        ESDescriptor esd = getEsDescriptor();
        if (esd != null) {
            return 4 + esd.getSize();
        } else {
            return 4 + data.remaining();
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        ESDescriptor esd = getEsDescriptor();
        if (esd != null) {
            byteBuffer.put((ByteBuffer) esd.serialize().rewind());
        } else {
            byteBuffer.put(data.duplicate());
        }
    }
}
