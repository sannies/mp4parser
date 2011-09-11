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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Abstract base class suitable for most boxes acting purely as container for other boxes.
 */
public abstract class AbstractContainerBox extends AbstractBox implements ContainerBox {
    protected List<Box> boxes = new LinkedList<Box>();

    @Override
    protected long getContentSize() {
        long contentSize = 0;
        for (Box boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public AbstractContainerBox(byte[] type) {
        super(type);
    }

    public List<Box> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }

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
                boxesToBeReturned.addAll(((ContainerBox) boxe).getBoxes(clazz, recursive));
            }
        }
        // Optimize here! Spare object creation work on arrays directly! System.arrayCopy
        return boxesToBeReturned;
        //return (T[]) boxesToBeReturned.toArray();
    }

    /**
     * Add <code>b</code> to the container and sets the parent correctly.
     *
     * @param b will be added to the container
     */
    public void addBox(Box b) {
        b.setParent(this);
        boxes.add(b);
    }

    public void removeBox(Box b) {
        boxes.remove(b);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {


        while (size >= 8) {
            long sp = in.position();
            Box box = boxParser.parseBox(in, this, lastMovieFragmentBox);
            long parsedBytes = in.position() - sp;
            assert parsedBytes == box.getSize() ||
                    box instanceof HandlerBox :
                    box + " didn't parse well. number of parsed bytes (" + parsedBytes + ") doesn't match getSize (" + box.getSize() + ")";
            size -= parsedBytes;

            boxes.add(box);
            //update field after each box
        }

    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        for (Box boxe : boxes) {
            boxe.getBox(os);
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(this.getClass().getSimpleName()).append("[");
        for (int i = 0; i < boxes.size(); i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(boxes.get(i).toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * The number of bytes from box start (first length byte) to the
     * first length byte of the first child box
     *
     * @return offset to first child box
     */
    public long getNumOfBytesToFirstChild() {
        return 8;
    }
}
