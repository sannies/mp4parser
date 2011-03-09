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
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class for all sample entries.
 *
 * @see com.coremedia.iso.boxes.sampleentry.AudioSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.VisualSampleEntry
 * @see com.coremedia.iso.boxes.rtp.RtpHintSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.TextSampleEntry
 */
public abstract class SampleEntry extends AbstractBox implements ContainerBox {
    private int dataReferenceIndex;
    protected Box[] boxes = new Box[0];
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

    public void addBox(AbstractBox b) {
        List<Box> listOfBoxes = new LinkedList<Box>(Arrays.asList(boxes));
        listOfBoxes.add(b);
        boxes = listOfBoxes.toArray(new AbstractBox[listOfBoxes.size()]);
    }

    public boolean removeBox(Box b) {
        List<Box> listOfBoxes = new LinkedList<Box>(Arrays.asList(boxes));
        boolean rc = listOfBoxes.remove(b);
        boxes = listOfBoxes.toArray(new Box[listOfBoxes.size()]);
        return rc;
    }

  @SuppressWarnings("unchecked")
  public <T extends Box> T[] getBoxes(Class<T> clazz, boolean recursive) {
    List<T> boxesToBeReturned = new ArrayList<T>(2);
    for (Box boxe : boxes) { //clazz.isInstance(boxe) / clazz == boxe.getClass()?
      if (clazz == boxe.getClass()) {
        boxesToBeReturned.add((T) boxe);
      }

      if (recursive && boxe instanceof ContainerBox) {
        boxesToBeReturned.addAll(Arrays.asList(((ContainerBox) boxe).getBoxes(clazz, recursive)));
      }
    }
    // Optimize here! Spare object creation work on arrays directly! System.arrayCopy
    return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
    //return (T[]) boxesToBeReturned.toArray();
  }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        byte[] tmp = in.read(6);
        assert Arrays.equals(new byte[6], tmp) : "reserved byte not 0";
        dataReferenceIndex = in.readUInt16();
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (Box box : boxes) {
            sizeOfChildren += box.getSize();
        }
        return getSize() - sizeOfChildren;
    }
}
