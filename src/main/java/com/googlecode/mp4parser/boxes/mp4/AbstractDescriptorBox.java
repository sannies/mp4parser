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

package com.googlecode.mp4parser.boxes.mp4;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BaseDescriptor;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.ObjectDescriptorFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ES Descriptor Box.
 */
public class AbstractDescriptorBox extends AbstractFullBox {
    private static Logger log = Logger.getLogger(AbstractDescriptorBox.class.getName());


    public BaseDescriptor descriptor;
    public byte[] data;

    public AbstractDescriptorBox(String type) {
        super(type);
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(data);
    }

    @Override
    protected long getContentSize() {
        //return 4 + esDescriptor.getSize();
        return descriptor.getSize();
    }

    public BaseDescriptor getDescriptor() {
        return descriptor;
    }

    public String getDescriptorAsString() {
        return descriptor.toString();
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);

        //read the descriptor and copy it to avoid broken descriptors reading beyond the box
        data = in.read((int) size - 4);
        final IsoBufferWrapper descriptorBuffer = new IsoBufferWrapperImpl(ByteBuffer.wrap(data));
        try {
            descriptor = ObjectDescriptorFactory.createFrom(-1, descriptorBuffer, descriptorBuffer.size());
        } catch (IOException e) {
            log.log(Level.WARNING, "Error parsing ObjectDescriptor", e);
            //that's why we copied it ;)
        } catch (IndexOutOfBoundsException e) {
            log.log(Level.WARNING, "Error parsing ObjectDescriptor", e);
            //that's why we copied it ;)
        }
    }
}
