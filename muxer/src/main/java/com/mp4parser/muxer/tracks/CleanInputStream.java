package com.mp4parser.muxer.tracks;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
* Removes NAL Unit emulation_prevention_three_byte.
*/
public class CleanInputStream extends FilterInputStream {

    int prevprev = -1;
    int prev = -1;

    public CleanInputStream(InputStream in) {
        super(in);
    }

    public boolean markSupported() {
        return false;
    }

    public int read() throws IOException {
        int c = super.read();
        if (c == 3 && prevprev == 0 && prev == 0) {
            // discard this character
            prevprev = -1;
            prev = -1;
            c = super.read();
        }
        prevprev = prev;
        prev = c;
        return c;
    }

    /**
     * Copy of InputStream.read(b, off, len)
     *
     * @see java.io.InputStream#read()
     */
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte) c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte) c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

}
