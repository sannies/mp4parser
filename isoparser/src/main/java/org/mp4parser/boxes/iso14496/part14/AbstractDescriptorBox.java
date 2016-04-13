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

import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BaseDescriptor;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.ObjectDescriptorFactory;
import org.mp4parser.support.AbstractFullBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ES Descriptor Box.
 */
public class AbstractDescriptorBox extends AbstractFullBox {
    private static Logger LOG = LoggerFactory.getLogger(AbstractDescriptorBox.class.getName());


    protected BaseDescriptor descriptor;
    protected ByteBuffer data;

    public AbstractDescriptorBox(String type) {
        super(type);
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        data.rewind(); // has been fforwarded by parsing
        byteBuffer.put(data);
    }

    @Override
    protected long getContentSize() {
        return 4 + data.limit();
    }

    public BaseDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(BaseDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescriptorAsString() {
        return descriptor.toString();
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        data = content.slice();
        content.position(content.position() + content.remaining());
        try {
            data.rewind();
            descriptor = ObjectDescriptorFactory.createFrom(-1, data.duplicate());
        } catch (IOException e) {
            LOG.warn("Error parsing ObjectDescriptor", e);
            //that's why we copied it ;)
        } catch (IndexOutOfBoundsException e) {
            LOG.warn("Error parsing ObjectDescriptor", e);
            //that's why we copied it ;)
        }

    }

}
