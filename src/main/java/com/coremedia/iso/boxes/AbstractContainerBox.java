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

import com.coremedia.iso.BoxParser;
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
public abstract class AbstractContainerBox extends Box implements ContainerBox {
    protected BoxInterface[] boxes;

    protected long getContentSize() {
        long contentSize = 0;
        for (BoxInterface boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public AbstractContainerBox(byte[] type) {
        super(type);
        boxes = new Box[0];
    }

    public BoxInterface[] getBoxes() {
        return boxes;
    }

    @SuppressWarnings("unchecked")
    public <T extends BoxInterface> T[] getBoxes(Class<T> clazz) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (BoxInterface boxe : boxes) {
            if (clazz == boxe.getClass()) {
                boxesToBeReturned.add((T) boxe);
            }
        }
        // Optimize here! Spare object creation work on arrays directly! System.arrayCopy
        return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
        //return (T[]) boxesToBeReturned.toArray();
    }

    public void addBox(BoxInterface b) {
        List<BoxInterface> listOfBoxes = new LinkedList<BoxInterface>(Arrays.asList(boxes));
        listOfBoxes.add(b);
        boxes = listOfBoxes.toArray(new Box[listOfBoxes.size()]);
    }

    public void removeBox(BoxInterface b) {
        List<BoxInterface> listOfBoxes = new LinkedList<BoxInterface>(Arrays.asList(boxes));
        listOfBoxes.remove(b);
        boxes = listOfBoxes.toArray(new BoxInterface[listOfBoxes.size()]);
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        List<BoxInterface> boxeList = new LinkedList<BoxInterface>();

        while (size > 8) {
            long sp = in.position();
            BoxInterface box = boxParser.parseBox(in, this, lastMovieFragmentBox);
            long parsedBytes = in.position() - sp;
            assert parsedBytes == box.getSize() :
                    box + " didn't parse well. number of parsed bytes (" + parsedBytes + ") doesn't match getSize (" + box.getSize() + ")";
            size -= box.getSize();

            boxeList.add(box);
            //update field after each box
            this.boxes = boxeList.toArray(new BoxInterface[boxeList.size()]);
        }

    }

    protected void getContent(IsoOutputStream os) throws IOException {
        for (BoxInterface boxe : boxes) {
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
