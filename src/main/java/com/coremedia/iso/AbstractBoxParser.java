/*
 * Copyright 2012 Sebastian Annies, Hamburg
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
package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.UserBox;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.googlecode.mp4parser.DataSource;

import java.util.logging.Logger;

/**
 * This BoxParser handles the basic stuff like reading size and extracting box type.
 */
public abstract class AbstractBoxParser implements BoxParser {

    private static Logger LOG = Logger.getLogger(AbstractBoxParser.class.getName());

    public abstract Box createBox(String type, byte[] userType, String parent);

    ThreadLocal<ByteBuffer> header = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(32);
        }
    };


    /**
     * Parses the next size and type, creates a box instance and parses the box's content.
     *
     * @param byteChannel the DataSource pointing to the ISO file
     * @param parent      the current box's parent (null if no parent)
     * @return the box just parsed
     * @throws java.io.IOException if reading from <code>in</code> fails
     */
    public Box parseBox(DataSource byteChannel, Container parent) throws IOException {
        header.get().rewind().limit(8);
        int bytesRead = 0;


        while ((bytesRead += byteChannel.read(header.get())) != 8) {
            if (bytesRead < 0) {
                throw new EOFException();
            }
        }
        header.get().rewind();

        long size = IsoTypeReader.readUInt32(header.get());
        // do plausibility check
        if (size < 8 && size > 1) {
            LOG.severe("Plausibility check failed: size < 8 (size = " + size + "). Stop parsing!");
            return null;
        }


        String type = IsoTypeReader.read4cc(header.get());
        //System.err.println(type);
        byte[] usertype = null;
        long contentSize;

        if (size == 1) {
            header.get().limit(16);
            byteChannel.read(header.get());
            header.get().position(8);
            size = IsoTypeReader.readUInt64(header.get());
            contentSize = size - 16;
        } else if (size == 0) {
            size = byteChannel.size() - byteChannel.position() - 8;
            contentSize = size - 8;
            throw new RuntimeException( "'" + type + "' with '" + (parent instanceof Box?((Box) parent).getType():"IsoFile") + "' as parent has length == 0. That's not supported");
        } else {
            contentSize = size - 8;
        }
        if (UserBox.TYPE.equals(type)) {
            header.get().limit(header.get().limit() + 16);
            byteChannel.read(header.get());
            usertype = new byte[16];
            for (int i = header.get().position() - 16; i < header.get().position(); i++) {
                usertype[i - (header.get().position() - 16)] = header.get().get(i);
            }
            contentSize -= 16;
        }
        Box box = createBox(type, usertype, (parent instanceof Box) ? ((Box) parent).getType() : "");
        box.setParent(parent);
        //LOG.finest("Parsing " + box.getType());
        // System.out.println("parsing " + Arrays.toString(box.getType()) + " " + box.getClass().getName() + " size=" + size);
        header.get().rewind();

        box.parse(byteChannel, header.get(), contentSize, this);
        return box;
    }


}
