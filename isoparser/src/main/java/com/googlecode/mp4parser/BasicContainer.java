package com.googlecode.mp4parser;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.util.LazyList;
import com.googlecode.mp4parser.util.Logger;
import com.mp4parser.LightBox;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;

public class BasicContainer implements Container {
    private List<LightBox> boxes = new ArrayList<LightBox>();

    public BasicContainer() {
    }

    public List<LightBox> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<? extends LightBox> boxes) {
        this.boxes = new ArrayList<LightBox>(boxes);
    }

    protected long getContainerSize() {
        long contentSize = 0;
        for (int i = 0; i < getBoxes().size(); i++) {
            // it's quicker to iterate an array list like that since no iterator
            // needs to be instantiated
            contentSize += boxes.get(i).getSize();
        }
        return contentSize;
    }

    @SuppressWarnings("unchecked")
    public <T extends LightBox> List<T> getBoxes(Class<T> clazz) {
        List<T> boxesToBeReturned = null;
        T oneBox = null;
        List<LightBox> boxes = getBoxes();
        for (LightBox boxe : boxes) {
            //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            // I hereby finally decide to use isInstance

            if (clazz.isInstance(boxe)) {
                if (oneBox == null) {
                    oneBox = (T) boxe;
                } else {
                    if (boxesToBeReturned == null) {
                        boxesToBeReturned = new ArrayList<T>(2);
                        boxesToBeReturned.add(oneBox);
                    }
                    boxesToBeReturned.add((T) boxe);
                }
            }
        }
        if (boxesToBeReturned != null) {
            return boxesToBeReturned;
        } else if (oneBox != null) {
            return Collections.singletonList(oneBox);
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends LightBox> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        List<LightBox> boxes = getBoxes();
        for (int i = 0; i < boxes.size(); i++) {
            LightBox boxe = boxes.get(i);
            //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            // I hereby finally decide to use isInstance

            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add((T) boxe);
            }

            if (recursive && boxe instanceof Container) {
                boxesToBeReturned.addAll(((Container) boxe).getBoxes(clazz, recursive));
            }
        }
        return boxesToBeReturned;
    }

    /**
     * Add <code>box</code> to the container and sets the parent correctly. If <code>box</code> is <code>null</code>
     * nochange will be performed and no error thrown.
     *
     * @param box will be added to the container
     */
    public void addBox(LightBox box) {
        if (box != null) {
            boxes = new ArrayList<LightBox>(getBoxes());
            boxes.add(box);
        }
    }

    public void initContainer(ReadableByteChannel readableByteChannel, long containerSize, BoxParser boxParser) throws IOException {
        long contentProcessed = 0;

        while (containerSize < 0 || contentProcessed < containerSize) {
            try {
                Box b = boxParser.parseBox(readableByteChannel, (this instanceof Box) ? ((Box) this).getType() : null);
                boxes.add(b);
                contentProcessed += b.getSize();
            } catch (EOFException e) {
                if (containerSize < 0) {
                    return;
                } else {
                    throw e;
                }
            }
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


    public final void writeContainer(WritableByteChannel bb) throws IOException {
        for (LightBox box : getBoxes()) {
            box.getBox(bb);
        }
    }
}
