/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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
import com.coremedia.iso.boxes.MovieBox;
import com.googlecode.mp4parser.annotations.DoNotParseDetail;
import com.googlecode.mp4parser.util.LazyList;
import com.googlecode.mp4parser.util.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;

/**
 * The most upper container for ISO Boxes. It is a container box that is a file.
 * Uses IsoBufferWrapper  to access the underlying file.
 */
@DoNotParseDetail
public class IsoFile implements Closeable, Iterator<Box>, ContainerBox {
    private static Logger LOG = Logger.getLogger(IsoFile.class);
    protected BoxParser boxParser = createBoxParser();
    ReadableByteChannel byteChannel;
    long position = 0;
    List<Box> boxes = new LinkedList<Box>();

    public IsoFile() {
    }

    /**
     * Shortcut constructor that creates a <code>FileChannel</code> from the
     * given filename and pass it to the {@link IsoFile#IsoFile(java.nio.channels.FileChannel)}
     * constructor.
     *
     * @param filename of the MP4 file to be parsed
     * @throws IOException in case I/O error
     */
    public IsoFile(String filename) throws IOException {
        this(new FileInputStream(filename).getChannel());
    }

    /**
     * Creates a new <code>IsoFile</code> from a <code>FileChannel</code>. Uses memory-mapping
     * to save heap memory.
     *
     * @param fileChannel the source file
     * @throws IOException in case I/O error
     */
    public IsoFile(FileChannel fileChannel) throws IOException {
        this((ReadableByteChannel) fileChannel);
    }

    /**
     * Creates a new <code>IsoFile</code> from a <code>ReadableByteChannel</code>.
     * <p/>
     * Try to use {@link IsoFile#IsoFile(FileChannel)} so you can benefit from
     * {@link FileChannel#map(java.nio.channels.FileChannel.MapMode, long, long)}. It will
     * reduce your heap requirements drastically!
     *
     * @param byteChannel the data source
     * @throws IOException in case I/O error
     * @deprecated use {@link IsoFile#IsoFile(FileChannel)} to save heap
     */
    public IsoFile(ReadableByteChannel byteChannel) throws IOException {
        this.byteChannel = byteChannel;
    }

    public IsoFile(ReadableByteChannel byteChannel, BoxParser boxParser) throws IOException {
        this.byteChannel = byteChannel;
        this.boxParser = boxParser;
    }

    protected BoxParser createBoxParser() {
        return new PropertyBoxParserImpl();
    }

    public List<Box> getBoxes() {
        if (byteChannel != null) {
            return new LazyList<Box>(boxes, this);
        } else {
            return boxes;
        }
    }


    public void remove() {
        throw new UnsupportedOperationException();
    }

    Box lookahead = null;


    public boolean hasNext() {

        if (lookahead != null) {
            return true;
        } else {
            try {
                lookahead = next();
                return true;
            } catch (NoSuchElementException e) {
                return false;
            }
        }
    }

    public synchronized Box next() {

        if (lookahead != null) {
            Box b = lookahead;
            lookahead = null;
            return b;
        } else {
            LOG.logDebug("Parsing next() box");
            if (byteChannel == null) {
                throw new NoSuchElementException();
            }
            try {
                if (byteChannel instanceof FileChannel) {
                    ((FileChannel) byteChannel).position(position);
                }
                Box b = boxParser.parseBox(byteChannel, this);

                if (byteChannel instanceof FileChannel) {
                    position = ((FileChannel) byteChannel).position();
                }
                return b;
            } catch (EOFException e) {
                throw new NoSuchElementException();
            } catch (IOException e) {
                throw new NoSuchElementException();
            }
        }

    }

    @DoNotParseDetail
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("IsoFile[");
        if (boxes == null) {
            buffer.append("unparsed");
        } else {
            for (int i = 0; i < boxes.size(); i++) {
                if (i > 0) {
                    buffer.append(";");
                }
                buffer.append(boxes.get(i).toString());
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    @DoNotParseDetail
    public static byte[] fourCCtoBytes(String fourCC) {
        byte[] result = new byte[4];
        if (fourCC != null) {
            for (int i = 0; i < Math.min(4, fourCC.length()); i++) {
                result[i] = (byte) fourCC.charAt(i);
            }
        }
        return result;
    }

    @DoNotParseDetail
    public static String bytesToFourCC(byte[] type) {
        byte[] result = new byte[]{0, 0, 0, 0};
        if (type != null) {
            System.arraycopy(type, 0, result, 0, Math.min(type.length, 4));
        }
        try {
            return new String(result, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character encoding is missing", e);
        }
    }


    public long getSize() {
        long size = 0;
        for (Box box : boxes) {
            size += box.getSize();
        }
        return size;
    }

    public IsoFile getIsoFile() {
        return this;
    }


    /**
     * Shortcut to get the MovieBox since it is often needed and present in
     * nearly all ISO 14496 files (at least if they are derived from MP4 ).
     *
     * @return the MovieBox or <code>null</code>
     */
    @DoNotParseDetail
    public MovieBox getMovieBox() {
        for (Box box : getBoxes()) {
            if (box instanceof MovieBox) {
                return (MovieBox) box;
            }
        }
        return null;
    }

    public void getBox(WritableByteChannel os) throws IOException {
        for (Box box : getBoxes()) {

            if (os instanceof FileChannel) {
                long startPos = ((FileChannel) os).position();
                box.getBox(os);
                long size = ((FileChannel) os).position() - startPos;
                assert size == box.getSize() : box.getType() + " Size: " + size + " box.getSize(): " + box.getSize();
            } else {
                box.getBox(os);
            }

        }
    }

    public void close() throws IOException {
        this.byteChannel.close();
    }

    public void setBoxes(List<Box> boxes) {
        byteChannel = null;
        this.boxes = boxes;
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (Box boxe : getBoxes()) {
            //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            // I hereby finally decide to use isInstance

            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add((T) boxe);
            }

            if (recursive && boxe instanceof ContainerBox) {
                boxesToBeReturned.addAll(((ContainerBox) boxe).getBoxes(clazz, recursive));
            }
        }
        return boxesToBeReturned;
    }

    public ContainerBox getParent() {
        return null;
    }

    public void setParent(ContainerBox parent) {
        throw new UnsupportedOperationException();
    }

    public String getType() {
        return null;
    }

    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void addBox(Box box) {
        while (hasNext()) {
            boxes.add(next());
        }
        boxes.add(box);
        box.setParent(this);
    }
}
