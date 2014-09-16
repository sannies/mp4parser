package com.googlecode.mp4parser;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.util.LazyList;
import com.googlecode.mp4parser.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 18.05.13.
 */
public class BasicContainer implements Container, Iterator<Box>, Closeable {
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
    private static Logger LOG = Logger.getLogger(BasicContainer.class);
    protected BoxParser boxParser;
    protected DataSource dataSource;
    Box lookahead = null;
    long parsePosition = 0;
    long startPosition = 0;
    long endPosition = 0;
    private List<Box> boxes = new ArrayList<Box>();

    public BasicContainer() {
    }

    public List<Box> getBoxes() {
        if (dataSource != null && lookahead != EOF) {
            return new LazyList<Box>(boxes, this);
        } else {
            return boxes;
        }
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = new ArrayList<Box>(boxes);
        this.lookahead = EOF;
        this.dataSource = null;
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
    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        List<T> boxesToBeReturned = null;
        T oneBox = null;
        List<Box> boxes = getBoxes();
        for (int i = 0; i < boxes.size(); i++) {
            Box boxe = boxes.get(i);
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
    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        List<Box> boxes = getBoxes();
        for (int i = 0; i < boxes.size(); i++) {
            Box boxe = boxes.get(i);
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
    public void addBox(Box box) {
        if (box != null) {
            boxes = new ArrayList<Box>(getBoxes());
            box.setParent(this);
            boxes.add(box);
        }
    }

    public void initContainer(DataSource dataSource, long containerSize, BoxParser boxParser) throws IOException {

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
           // LOG.logDebug("Parsing next() box");
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

    public ByteBuffer getByteBuffer(long rangeStart, long size) throws IOException {
        if (this.dataSource != null) {
            synchronized (this.dataSource) {
                return this.dataSource.map(this.startPosition + rangeStart, size);
            }
        } else {
            ByteBuffer out = ByteBuffer.allocate(l2i(size));
            long rangeEnd = rangeStart + size;
            long boxStart;
            long boxEnd = 0;
            for (Box box : boxes) {
                boxStart = boxEnd;
                boxEnd = boxStart + box.getSize();
                if (!(boxEnd <= rangeStart || boxStart >= rangeEnd)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    WritableByteChannel wbc = Channels.newChannel(baos);
                    box.getBox(wbc);
                    wbc.close();

                    if (boxStart >= rangeStart && boxEnd <= rangeEnd) {
                        out.put(baos.toByteArray());
                        // within -> use full box
                    } else if (boxStart < rangeStart && boxEnd > rangeEnd) {
                        // around -> use 'middle' of box
                        int length = l2i(box.getSize() - (rangeStart - boxStart) - (boxEnd - rangeEnd));
                        out.put(baos.toByteArray(), l2i(rangeStart - boxStart), length);
                    } else if (boxStart < rangeStart && boxEnd <= rangeEnd) {
                        // endwith
                        int length = l2i(box.getSize() - (rangeStart - boxStart));
                        out.put(baos.toByteArray(), l2i(rangeStart - boxStart), length);
                    } else if (boxStart >= rangeStart && boxEnd > rangeEnd) {
                        int length = l2i(box.getSize() - (boxEnd - rangeEnd));
                        out.put(baos.toByteArray(), 0, length);
                    }
                }
            }
            return (ByteBuffer) out.rewind();
        }
    }

    public void close() throws IOException {
        dataSource.close();
    }
}
