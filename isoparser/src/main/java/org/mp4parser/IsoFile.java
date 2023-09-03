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

package org.mp4parser;

import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.support.DoNotParseDetail;

import java.io.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * The most upper container for ISO Boxes. It is a container box that is a file.
 * Uses IsoBufferWrapper  to access the underlying file.
 */
@DoNotParseDetail
public class IsoFile extends BasicContainer implements Closeable {
    private final ReadableByteChannel readableByteChannel;

    private FileInputStream fis;


    public IsoFile(String file) throws IOException {
        this(new File(file));
    }

    public IsoFile(File file) throws IOException {
        this.fis = new FileInputStream(file);
        this.readableByteChannel = fis.getChannel();
        initContainer(readableByteChannel, -1, new PropertyBoxParserImpl());
    }

    /**
     * @param readableByteChannel the data source
     * @throws IOException in case I/O error
     */
    public IsoFile(ReadableByteChannel readableByteChannel) throws IOException {
        this(readableByteChannel, new PropertyBoxParserImpl());
    }

    public IsoFile(ReadableByteChannel readableByteChannel, BoxParser boxParser) throws IOException {
        this.readableByteChannel = readableByteChannel;
        initContainer(readableByteChannel, -1, boxParser);
    }

    public static byte[] fourCCtoBytes(String fourCC) {
        byte[] result = new byte[4];
        if (fourCC != null) {
            for (int i = 0; i < Math.min(4, fourCC.length()); i++) {
                result[i] = (byte) fourCC.charAt(i);
            }
        }
        return result;
    }

    public static String bytesToFourCC(byte[] type) {
        byte[] result = new byte[]{0, 0, 0, 0};
        if (type != null) {
            System.arraycopy(type, 0, result, 0, Math.min(type.length, 4));
        }
        return new String(result, StandardCharsets.ISO_8859_1);
    }


    public long getSize() {
        return getContainerSize();
    }


    /**
     * Shortcut to get the MovieBox since it is often needed and present in
     * nearly all ISO 14496 files (at least if they are derived from MP4 ).
     *
     * @return the MovieBox or <code>null</code>
     */
    public MovieBox getMovieBox() {
        for (Box box : getBoxes()) {
            if (box instanceof MovieBox) {
                return (MovieBox) box;
            }
        }
        return null;
    }

    public void getBox(WritableByteChannel os) throws IOException {
        writeContainer(os);
    }

    @Override
    public void close() throws IOException {
        this.readableByteChannel.close();
        if (this.fis != null) {
            this.fis.close();
        }
        for (Box box : getBoxes()) {
            if (box instanceof Closeable) {
                ((Closeable) box).close();
            }
        }
    }

    @Override
    public String toString() {
        return "model(" + readableByteChannel + ")";
    }
}
