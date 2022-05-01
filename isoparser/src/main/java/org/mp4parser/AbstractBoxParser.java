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
package org.mp4parser;

import org.mp4parser.boxes.UserBox;
import org.mp4parser.tools.IsoTypeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;

/**
 * This BoxParser handles the basic stuff like reading size and extracting box type.
 */
public abstract class AbstractBoxParser implements BoxParser {

    private List<String> skippedTypes;
    
    private static Logger LOG = LoggerFactory.getLogger(AbstractBoxParser.class.getName());
    ThreadLocal<ByteBuffer> header = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(32);
        }
    };

    public abstract ParsableBox createBox(String type, byte[] userType, String parent);

    /**
     * Parses the next size and type, creates a box instance and parses the box's content.
     *
     * @param byteChannel the DataSource pointing to the ISO file
     * @param parentType  the current box's parent's type (null if no parent)
     * @return the box just parsed
     * @throws java.io.IOException if reading from <code>in</code> fails
     */
    public ParsableBox parseBox(ReadableByteChannel byteChannel, String parentType) throws IOException {
        ((Buffer)header.get()).rewind().limit(8);

        int bytesRead = 0;
        int b;
        while ((b = byteChannel.read(header.get())) + bytesRead < 8) {
            if (b < 0) {
                throw new EOFException();
            } else {
                bytesRead += b;
            }
        }
        ((Buffer)header.get()).rewind();

        long size = IsoTypeReader.readUInt32(header.get());
        // do plausibility check
        if (size < 8 && size > 1) {
            LOG.error("Plausibility check failed: size < 8 (size = {}). Stop parsing!", size);
            return null;
        }


        String type = IsoTypeReader.read4cc(header.get());
        //System.err.println(type);
        byte[] usertype = null;
        long contentSize;

        if (size == 1) {
            ((Buffer)header.get()).limit(16);
            byteChannel.read(header.get());
            ((Buffer)header.get()).position(8);
            size = IsoTypeReader.readUInt64(header.get());
            contentSize = size - 16;
        } else if (size == 0) {
            throw new RuntimeException("box size of zero means 'till end of file. That is not yet supported");
        } else {
            contentSize = size - 8;
        }
        if (UserBox.TYPE.equals(type)) {
            ((Buffer)header.get()).limit(((Buffer)header.get()).limit() + 16);
            byteChannel.read(header.get());
            usertype = new byte[16];
            for (int i = ((Buffer)header.get()).position() - 16; i < ((Buffer)header.get()).position(); i++) {
                usertype[i - (((Buffer)header.get()).position() - 16)] = header.get().get(i);
            }
            contentSize -= 16;
        }
        ParsableBox parsableBox = null;
        if( skippedTypes != null && skippedTypes.contains(type) ) {
            LOG.trace("Skipping box {} {} {}", type, usertype, parentType);
            parsableBox = new SkipBox(type, usertype, parentType);
        }
        else {
            LOG.trace("Creating box {} {} {}", type, usertype, parentType);
            parsableBox = createBox(type, usertype, parentType);
        }
        //LOG.finest("Parsing " + box.getType());
        // System.out.println("parsing " + Mp4Arrays.toString(box.getType()) + " " + box.getClass().getName() + " size=" + size);
        ((Buffer)header.get()).rewind();

        parsableBox.parse(byteChannel, header.get(), contentSize, this);
        return parsableBox;
    }

    public AbstractBoxParser skippingBoxes(String... types) {
        skippedTypes = Arrays.asList(types);
        return this;
    }
}
