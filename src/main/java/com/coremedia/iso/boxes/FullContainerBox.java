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
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class for a full iso box only containing ither boxes.
 */
public abstract class FullContainerBox extends FullBox implements ContainerBox {
    protected BoxInterface[] boxes;


    @SuppressWarnings("unchecked")
    public <T extends BoxInterface> T[] getBoxes(Class<T> clazz) {
        ArrayList<T> boxesToBeReturned = new ArrayList<T>();
        for (BoxInterface boxe : boxes) {
            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add(clazz.cast(boxe));
            }
        }
        return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
    }

    protected long getContentSize() {
        long contentSize = 0;
        for (BoxInterface boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public void addBox(BoxInterface b) {
        List<BoxInterface> listOfBoxes = new LinkedList<BoxInterface>(Arrays.asList(boxes));
        listOfBoxes.add(b);
        boxes = listOfBoxes.toArray(new Box[listOfBoxes.size()]);
    }

    public void removeBox(BoxInterface b) {
        List<BoxInterface> listOfBoxes = new LinkedList<BoxInterface>(Arrays.asList(boxes));
        listOfBoxes.remove(b);
        boxes = listOfBoxes.toArray(new Box[listOfBoxes.size()]);
    }

    public FullContainerBox(String type) {
        super(IsoFile.fourCCtoBytes(type));
        boxes = new Box[0];
    }

    public BoxInterface[] getBoxes() {
        return boxes;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        parseHeader(in, size);
        parseBoxes(size, in, boxParser, lastMovieFragmentBox);
    }

    protected void parseBoxes(long size, IsoBufferWrapper in, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        List<Box> boxeList = new LinkedList<Box>();
        long remainingContentSize = size - 4;
        while (remainingContentSize > 0) {
            Box box = boxParser.parseBox(in, this, lastMovieFragmentBox);
            remainingContentSize -= box.getSize();
            boxeList.add(box);
        }
        this.boxes = boxeList.toArray(new Box[boxeList.size()]);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getDisplayName()).append("[");
        BoxInterface[] boxes2 = getBoxes();
        for (int i = 0; i < boxes2.length; i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(boxes2[i].toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        for (BoxInterface boxe : boxes) {
            boxe.getBox(os);
        }
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (BoxInterface box : boxes) {
            sizeOfChildren += box.getSize();
        }
        return getSize() - sizeOfChildren;
    }
}
