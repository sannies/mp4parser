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
import com.coremedia.iso.boxes.MediaDataBox;
import com.coremedia.iso.boxes.TrackMetaDataContainer;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.Sample;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The most upper container for ISO Boxes. It is a container box that is a file.
 * Uses IsoBufferWrapper  to access the underlying file.
 */
public class IsoFile implements ContainerBox, Box {
    private Box[] boxes = new Box[0];
    private BoxParser boxParser = new PropertyBoxParserImpl();
    private IsoBufferWrapper originalIso;


    public IsoFile(IsoBufferWrapper originalIso) {
        this.originalIso = originalIso;
    }

    public IsoFile(IsoBufferWrapper originalIso, BoxParser boxParser) {
        this.originalIso = originalIso;
        this.boxParser = boxParser;
    }

    public ContainerBox getParent() {
        return null;
    }

    public byte[] getType() {
        return new byte[0];
    }

    public boolean isFragmented() {
        MovieExtendsBox[] movieExtendsBoxes = getBoxes(MovieExtendsBox.class);
        return movieExtendsBoxes != null && movieExtendsBoxes.length > 0;
    }


    public Box[] getBoxes() {
        return boxes;
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> T[] getBoxes(Class<T> clazz) {
        ArrayList<T> boxesToBeReturned = new ArrayList<T>();
        for (Box boxe : boxes) {
            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add(clazz.cast(boxe));
            }
        }
        return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
    }


    public void parse() throws IOException {

        List<Box> boxeList = new LinkedList<Box>();
        boolean done = false;
        Box lastMovieFragmentBox = null;
        while (!done) {
            long sp = originalIso.position();
            if (originalIso.remaining() >= 8) {
                Box box = boxParser.parseBox(originalIso, this, lastMovieFragmentBox);
                if (box instanceof MovieFragmentBox) lastMovieFragmentBox = box;
                boxeList.add(box);
                this.boxes = boxeList.toArray(new Box[boxeList.size()]);
                assert box.calculateOffset() == sp : "calculated offset differs from offset in file";
            } else {
                done = true;
            }
        }
    }

    public void parseMdats() throws IOException {
        MediaDataBox<? extends TrackMetaDataContainer>[] mdats = getBoxes(MediaDataBox.class);
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
            for (int i = 0; i < boxes.length; i++) {
                if (i > 0) {
                    buffer.append(";");
                }
                buffer.append(boxes[i].toString());
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
            for (int i = 0; i < Math.min(type.length, 4); i++) {
                result[i] = type[i];
            }
        }
        try {
            return new String(type, "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }


    /**
     * Writes the IsoFile.
     *
     * @param isos the target stream
     * @throws IOException in case of any error caused by the target stream or by reading the original ISO file.
     */
    public void getBox(IsoOutputStream isos) throws IOException {

        try {
            for (Box box : boxes) {
                box.getBox(isos);
            }
        } finally {
            isos.flush();
        }
    }

    /**
     * Returns the track with the given id as list of <code>Sample</code>. It unifies the track if it is spread over more
     * than one <code>MediaDataBox</code>.
     *
     * @param trackId as stated in ISO 14496-13
     * @return the track's list of <code>Sample</code>.
     */
    public List<Sample<? extends TrackMetaDataContainer>> getTrack(long trackId) {
        MediaDataBox<? extends TrackMetaDataContainer>[] mdats = getBoxes(MediaDataBox.class);
        List<Sample<? extends TrackMetaDataContainer>> samplesInTrack = new LinkedList<Sample<? extends TrackMetaDataContainer>>();
        for (MediaDataBox<? extends TrackMetaDataContainer> mdat : mdats) {
            List<? extends Chunk<? extends TrackMetaDataContainer>> chunks = mdat.getTrack(trackId).getChunks();
            for (Chunk<? extends TrackMetaDataContainer> chunk : chunks) {
                samplesInTrack.addAll(chunk.getSamples());
            }
        }
        return samplesInTrack;
    }

    public long getNumOfBytesToFirstChild() {
        return 0;
    }

    public long getSize() {
        long size = 0;
        for (Box box : boxes) {
            size += box.getSize();
        }
        return size;
    }

    public long calculateOffset() {
        return 0;
    }


}
