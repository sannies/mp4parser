package com.coremedia.iso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 9/19/11
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultiplexIsoBufferWrapperImpl extends AbstractIsoBufferWrapper {
    List<IsoBufferWrapper> multiplexees;
    int activeMultiplexee = 0;

    public MultiplexIsoBufferWrapperImpl(List<IsoBufferWrapper> multiplexees) {
        this.multiplexees = multiplexees;
    }


    public long position() throws IOException {
        if (activeMultiplexee >= 0) {
            long pos = 0;
            for (int i = 0; i < activeMultiplexee; i++) {
                pos += multiplexees.get(i).size();
            }
            pos += multiplexees.get(activeMultiplexee).position();
            return pos;
        } else {
            return size();
        }
    }

    public long remaining() throws IOException {
        if (activeMultiplexee == -1) {
            return 0;
        } else {
            long remaining = 0;
            for (int i = multiplexees.size() - 1; i > activeMultiplexee; i--) {
                remaining += multiplexees.get(i).size();
            }
            remaining += multiplexees.get(activeMultiplexee).remaining();
            return remaining;
        }
    }

    public void position(long position) throws IOException {
        if (position == size()) {
            activeMultiplexee = -1;
        } else {
            int current = 0;
            while (position >= multiplexees.get(current).size()) {
                position -= multiplexees.get(current++).size();
            }
            multiplexees.get(current).position((int) position);
            activeMultiplexee = current;
        }
    }


    public int read(byte[] b) throws IOException {
        for (int i = 0; i < b.length; i++) {
            b[i] = readByte();

        }
        return b.length;
    }


    public IsoBufferWrapper getSegment(long startPos, long length) throws IOException {
        long savePos = this.position();
        ArrayList<IsoBufferWrapper> segments = new ArrayList<IsoBufferWrapper>();
        position(startPos);
        while (length > 0) {
            IsoBufferWrapper currentSlice = multiplexees.get(activeMultiplexee);
            if (currentSlice.remaining() >= length) {
                segments.add(currentSlice.getSegment(currentSlice.position(), length));
                length -= length;
            } else {
                // ok use up current bytebuffer and jump to next
                length -= currentSlice.remaining();
                multiplexees.get(++activeMultiplexee).position(0);
                segments.add(currentSlice.getSegment(0, currentSlice.remaining()));
            }

        }
        position(savePos);
        return new MultiplexIsoBufferWrapperImpl(segments);
    }

    public int read() throws IOException {
        if (multiplexees.get(activeMultiplexee).remaining() == 0) {
            if (multiplexees.size() > activeMultiplexee + 1) {
                activeMultiplexee++;
                multiplexees.get(activeMultiplexee).position(0);
                return read();
            } else {
                return -1;
            }
        }
        int b = multiplexees.get(activeMultiplexee).read();
        return b < 0 ? b + 256 : b;
    }

    public long size() {
        long size = 0;
        for (IsoBufferWrapper multiplexee : multiplexees) {
            size += multiplexee.size();
        }
        return size;
    }

}
