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

import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.Sample;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The most upper container for ISO Boxes. It is a container box that is a file.
 * Uses IsoBufferWrapper  to access the underlying file.
 */
public class IsoFile extends AbstractContainerBox {
    protected BoxParser boxParser = new PropertyBoxParserImpl();
    protected IsoBufferWrapper originalIso;
    protected boolean parsed;
    private boolean mdatsParsed;

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
    public String getDisplayName() {
        return "file";
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

    public void parseMdats() throws IOException {
        List<MediaDataBox> mdats = getBoxes(MediaDataBox.class);
        for (MediaDataBox<? extends TrackMetaDataContainer> mdat : mdats) {
            mdat.parseTrackChunkSample();
        }
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
        char[] result = "\0\0\0\0".toCharArray();
        if (type != null) {
            for (int i = 0; i < Math.min(type.length, 4); i++) {
                result[i] = (char) type[i];
            }
        }
        return new String(result);
    }


    /**
     * Returns the track with the given id as list of <code>Sample</code>. It unifies the track if it is spread over more
     * than one <code>MediaDataBox</code>.
     *
     * @param trackId as stated in ISO 14496-13
     * @return the track's list of <code>Sample</code>.
     */
    public List<Sample<? extends TrackMetaDataContainer>> getTrack(long trackId) {
        List<MediaDataBox> mdats = getBoxes(MediaDataBox.class);
        List<Sample<? extends TrackMetaDataContainer>> samplesInTrack = new LinkedList<Sample<? extends TrackMetaDataContainer>>();
        for (MediaDataBox mdat : mdats) {
            List<Chunk> chunks = mdat.getTrack(trackId).getChunks();
            for (Chunk chunk : chunks) {
                samplesInTrack.addAll(chunk.getSamples());
            }
        }
        return samplesInTrack;
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
}
