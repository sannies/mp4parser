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

package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxInterface;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class for all sample entries.
 *
 * @see com.coremedia.iso.boxes.sampleentry.AudioSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.VisualSampleEntry
 * @see com.coremedia.iso.boxes.rtp.HintSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.TextSampleEntry
 */
public abstract class SampleEntry extends Box implements ContainerBox {
    private int dataReferenceIndex;
    protected BoxInterface[] boxes;
    byte[] type;

    protected SampleEntry(byte[] type) {
        super(type);
        this.type = type;
    }

    public byte[] getType() {
        return type;
    }

    public void setType(byte[] type) {
        this.type = type;
    }

    public int getDataReferenceIndex() {
        return dataReferenceIndex;
    }

    public void addBox(Box b) {
        List<BoxInterface> listOfBoxes = new LinkedList<BoxInterface>(Arrays.asList(boxes));
        listOfBoxes.add(b);
        boxes = listOfBoxes.toArray(new Box[listOfBoxes.size()]);
    }

    public boolean removeBox(BoxInterface b) {
        List<BoxInterface> listOfBoxes = new LinkedList<BoxInterface>(Arrays.asList(boxes));
        boolean rc = listOfBoxes.remove(b);
        boxes = listOfBoxes.toArray(new BoxInterface[listOfBoxes.size()]);
        return rc;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        byte[] tmp = in.read(6);
        assert Arrays.equals(new byte[6], tmp) : "reserved byte not 0";
        dataReferenceIndex = in.readUInt16();
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (BoxInterface box : boxes) {
            sizeOfChildren += box.getSize();
        }
        return getSize() - sizeOfChildren;
    }
}
