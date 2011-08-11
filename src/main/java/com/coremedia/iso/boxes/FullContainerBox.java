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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class for a full iso box only containing ither boxes.
 */
public abstract class FullContainerBox extends AbstractFullBox implements ContainerBox {
    protected List<Box> boxes = new LinkedList<Box>();


    public void setBoxes(List<Box> boxes) {
        this.boxes = new LinkedList<Box>(boxes);
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (Box boxe : boxes) { //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            if (clazz == boxe.getClass()) {
                boxesToBeReturned.add((T) boxe);
            }

            if (recursive && boxe instanceof ContainerBox) {
                boxesToBeReturned.addAll((((ContainerBox) boxe).getBoxes(clazz, recursive)));
            }
        }
        // Optimize here! Spare object creation work on arrays directly! System.arrayCopy
        return boxesToBeReturned;
        //return (T[]) boxesToBeReturned.toArray();
    }

    protected long getContentSize() {
        long contentSize = 0;
        for (Box boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public void addBox(Box b) {
        boxes.add(b);
    }

    public void removeBox(Box b) {
        boxes.remove(b);
    }

    public FullContainerBox(String type) {
        super(IsoFile.fourCCtoBytes(type));
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        long pos = in.position();
        int version = in.readUInt8();
        long flags = in.readUInt24();
        // perhaps we don't have a full box here. In that case the flags and version
        // will be in a non-plausible range.
        if (version > 3) {
            // that's not plausible
            if (flags > 7) {
                // and thats not plausible too
                // we have one of those stupid apple meta boxes
                version = 0;
                flags = 0;
                in.position(pos); // reset position to before trying to parse flags and vewrsion
            }

        }
        parseBoxes(size, in, boxParser, lastMovieFragmentBox);
    }

    protected void parseBoxes(long size, IsoBufferWrapper in, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        long remainingContentSize = size - 4;
        while (remainingContentSize > 0) {
            Box box = boxParser.parseBox(in, this, lastMovieFragmentBox);
            remainingContentSize -= box.getSize();
            boxes.add(box);
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("FreeBox[");
        for (int i = 0; i < boxes.size(); i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(boxes.get(i).toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        for (Box boxe : boxes) {
            boxe.getBox(os);
        }
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (Box box : boxes) {
            sizeOfChildren += box.getSize();
        }
        return getSize() - sizeOfChildren;
    }
}
