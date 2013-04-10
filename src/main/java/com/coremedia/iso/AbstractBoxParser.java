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
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.UserBox;
import com.googlecode.mp4parser.util.Path;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This BoxParser handles the basic stuff like reading size and extracting box type.
 */
public abstract class AbstractBoxParser implements BoxParser {

    private static Logger LOG = Logger.getLogger(AbstractBoxParser.class.getName());

    public abstract Box createBox(String type, byte[] userType, String parent);

    Map<String, Long> boxPositions = new HashMap<String, Long>();

    ThreadLocal<ByteBuffer> header = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(32);
        }
    };


    /**
     * Parses the next size and type, creates a box instance and parses the box's content.
     *
     * @param byteChannel the FileChannel pointing to the ISO file
     * @param parent      the current box's parent (null if no parent)
     * @return the box just parsed
     * @throws java.io.IOException if reading from <code>in</code> fails
     */
    public Box parseBox(ReadableByteChannel byteChannel, ContainerBox parent) throws IOException {
        long pos = -1;
        if (byteChannel instanceof FileChannel) {
            pos = ((FileChannel) byteChannel).position();
        }
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
            if (byteChannel instanceof FileChannel) {
                size = ((FileChannel) byteChannel).size() - ((FileChannel) byteChannel).position() - 8;
            } else {
                throw new RuntimeException("Only FileChannel inputs may use size == 0 (box reaches to the end of file)");
            }
            contentSize = size - 8;
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
        Box box = createBox(type, usertype, parent.getType());
        box.setParent(parent);
        //LOG.finest("Parsing " + box.getType());
        // System.out.println("parsing " + Arrays.toString(box.getType()) + " " + box.getClass().getName() + " size=" + size);
        header.get().rewind();

        box.parse(byteChannel, header.get(), contentSize, this);
//        System.out.println("box = " + box.getType());
        if (true) {
            int index = 0;
            List<Box> boxes;
            if (box.getParent() instanceof IsoFile) {
                boxes = ((IsoFile) box.getParent()).boxes;
            } else {
                boxes = box.getParent().getBoxes();
            }
            for (Box siblings : boxes) {
                if (siblings.getType().equals(box.getType())) {
                    index++;
                }
            }
            boxPositions.put(Path.createPath(box.getParent()) + "/" + type + "[" + index + "]", pos);
        }

        return box;
    }


}
