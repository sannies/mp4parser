package com.googlecode.mp4parser;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.util.LazyList;
import com.googlecode.mp4parser.util.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by sannies on 18.05.13.
 */
public class BasicContainer implements Container, Iterator<Box> {
    private static Logger LOG = Logger.getLogger(AbstractContainerBox.class);

    private List<Box> boxes = new ArrayList<Box>();
    protected BoxParser boxParser;
    protected DataSource dataSource;
    Box lookahead = null;
    long parsePosition = 0;
    long startPosition = 0;
    long endPosition = 0;

    private static final Box EOF = new AbstractBox("eof ") {

        @Override
        protected long getContentSize() {
            return 0;
        }

        @Override
        protected void getContent(ByteBuffer byteBuffer) {
        }

        @Override
        protected void _parseDetails(ByteBuffer content) {
        }
    };

    public List<Box> getBoxes() {
        if (dataSource != null && lookahead != EOF) {
            return new LazyList<Box>(boxes, this);
        } else {
            return boxes;
        }
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

    public BasicContainer() {
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = new ArrayList<Box>(boxes);
        this.lookahead = EOF;
        this.dataSource = null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (Box boxe : getBoxes()) {
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
     * Add <code>b</code> to the container and sets the parent correctly.
     *
     * @param b will be added to the container
     */
    public void addBox(Box b) {
        boxes = new ArrayList<Box>(getBoxes());
        b.setParent(this);
        boxes.add(b);
    }

    public void parseContainer(DataSource dataSource, long containerSize, BoxParser boxParser) throws IOException {

        this.dataSource = dataSource;
        this.parsePosition = this.startPosition = dataSource.position();
        dataSource.position(dataSource.position() + containerSize);
        this.endPosition = dataSource.position();
        this.boxParser = boxParser;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        if (lookahead == EOF) {
            return false;
        }
        if (lookahead != null) {
            return true;
        } else {
            try {
                lookahead = next();
                return true;
            } catch (NoSuchElementException e) {
                lookahead = EOF;
                return false;
            }
        }
    }

    public Box next() {
        if (lookahead != null && lookahead != EOF) {
            Box b = lookahead;
            lookahead = null;
            return b;
        } else {
            LOG.logDebug("Parsing next() box");
            if (dataSource == null || parsePosition >= endPosition) {
                lookahead = EOF;
                throw new NoSuchElementException();
            }

            try {
                synchronized (dataSource) {
                    dataSource.position(parsePosition);
                    Box b = boxParser.parseBox(dataSource, this);
                    //System.err.println(b.getType());
                    parsePosition = dataSource.position();
                    return b;
                }
            } catch (EOFException e) {
                throw new NoSuchElementException();
            } catch (IOException e) {
                throw new NoSuchElementException();
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
        for (Box box : getBoxes()) {
            box.getBox(bb);
        }
    }

    public ByteBuffer getByteBuffer(long start, long size) throws IOException {
        synchronized (this.dataSource) {
            return this.dataSource.map(this.startPosition + start, size);
        }
    }

}
