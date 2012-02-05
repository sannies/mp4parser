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

import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * The most upper container for ISO Boxes. It is a container box that is a file.
 * Uses IsoBufferWrapper  to access the underlying file.
 */
public class IsoFile extends AbstractContainerBox {
    protected BoxParser boxParser = new PropertyBoxParserImpl();
    protected IsoBufferWrapper originalIso;

    public IsoFile(IsoBufferWrapper originalIso) {
        super(new byte[]{});
        boxParser = createBoxParser();
        this.originalIso = originalIso;
    }

    public IsoFile(IsoBufferWrapper originalIso, BoxParser boxParser) {
        this(originalIso);
        this.boxParser = boxParser;
    }

    protected BoxParser createBoxParser() {
        return new PropertyBoxParserImpl();
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        throw new RuntimeException("This method is not meant to be used. Use parse() instead");
        //super.parse(in, size, boxParser, lastMovieFragmentBox);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void parse() throws IOException {

        boolean done = false;
        Box lastMovieFragmentBox = null;
        while (!done) {
            long sp = originalIso.position();
            if (originalIso.remaining() >= 8) {
                Box box = boxParser.parseBox(originalIso, this, lastMovieFragmentBox);
                if (box != null) {
                    if (box instanceof MovieFragmentBox) lastMovieFragmentBox = box;
                    boxes.add(box);
                    assert box.calculateOffset() == sp : "calculated offset differs from offset in file";
                } else {
                    done = true;
                }
            } else {
                done = true;
            }
        }
        parsed = done;
    }


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
        try {
            return new String(result, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character encoding is missing", e);
        }
    }


    @Override
    public long getNumOfBytesToFirstChild() {
        return 0;
    }

    @Override
    public long getSize() {
        long size = 0;
        for (Box box : boxes) {
            size += box.getSize();
        }
        return size;
    }

    @Override
    public long calculateOffset() {
        return 0;
    }

    @Override
    public long getOffset() {
        return 0;
    }

    @Override
    public IsoFile getIsoFile() {
        return this;
    }

    @Override
    protected long getHeaderSize() {
        return 0;
    }

    @Override
    public byte[] getHeader() {
        return new byte[0];
    }

    public BoxParser getBoxParser() {
        return boxParser;
    }

    public IsoBufferWrapper getOriginalIso() {
        return originalIso;
    }

    /**
     * Shortcut to get the MovieBox since it is often needed and present in
     * nearly all ISO 14496 files (at least if they are derived from MP4 ).
     *
     * @return the MovieBox or <code>null</code>
     */
    public MovieBox getMovieBox() {
        for (Box box : boxes) {
            if (box instanceof MovieBox) {
                return (MovieBox) box;
            }
        }
        return null;
    }
}
