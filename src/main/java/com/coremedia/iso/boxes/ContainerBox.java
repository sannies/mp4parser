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

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * Abstract base class suitable for most boxes acting purely as container for other boxes.
 */
public abstract class ContainerBox extends Box implements BoxContainer {
  protected Box[] boxes;

  protected long getContentSize() {
    long contentSize = 0;
    for (Box boxe : boxes) {
      contentSize += boxe.getSize();
    }
    return contentSize;
  }

  public ContainerBox(byte[] type) {
    super(type);
    boxes = new Box[0];
  }

  public Box[] getBoxes() {
    return boxes;
  }

  @SuppressWarnings("unchecked")
  public <T extends Box> T[] getBoxes(Class<T> clazz) {
    List<T> boxesToBeReturned = new ArrayList<T>(2);
    for (Box boxe : boxes) {
      if (clazz == boxe.getClass()) {
        boxesToBeReturned.add((T) boxe);
      }
    }
    // Optimize here! Spare object creation work on arrays directly! System.arrayCopy
    return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
    //return (T[]) boxesToBeReturned.toArray();
  }

  public void addBox(Box b) {
    List<Box> listOfBoxes = new LinkedList<Box>(Arrays.asList(boxes));
    listOfBoxes.add(b);
    boxes = listOfBoxes.toArray(new Box[listOfBoxes.size()]);
  }

  public void removeBox(Box b) {
    List<Box> listOfBoxes = new LinkedList<Box>(Arrays.asList(boxes));
    listOfBoxes.remove(b);
    boxes = listOfBoxes.toArray(new Box[listOfBoxes.size()]);
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    List<Box> boxeList = new LinkedList<Box>();

    while (size > 8) {
      long sp = in.position();
      Box box = boxFactory.parseBox(in, this, lastMovieFragmentBox);
      long parsedBytes = in.position() - sp;
      assert parsedBytes == box.getSize() :
              "number of parsed bytes (" + parsedBytes + ") of " + box.getDisplayName() + " doesn't match getSize (" + box.getSize() + ")";
      size -= box.getSize();

      boxeList.add(box);
      //update field after each box
      this.boxes = boxeList.toArray(new Box[boxeList.size()]);
    }

  }

  protected void getContent(IsoOutputStream os) throws IOException {
    for (Box boxe : boxes) {
      boxe.getBox(os);
    }
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(this.getClass().getSimpleName()).append("[");
    for (int i = 0; i < boxes.length; i++) {
      if (i > 0) {
        buffer.append(";");
      }
      buffer.append(boxes[i].toString());
    }
    buffer.append("]");
    return buffer.toString();
  }

  public long getNumOfBytesToFirstChild() {
    return 8;
  }
}
