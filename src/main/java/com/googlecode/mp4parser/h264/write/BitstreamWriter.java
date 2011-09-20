package com.googlecode.mp4parser.h264.write;

import com.googlecode.mp4parser.h264.Debug;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A dummy implementation of H264 RBSP output stream
 *
 * @author Stanislav Vitvitskiy
 */
public class BitstreamWriter {

    private final OutputStream os;
    private int[] curByte = new int[8];
    private int curBit;

    public BitstreamWriter(OutputStream out) {
        this.os = out;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ua.org.jplayer.javcodec.h264.H264BitOutputStream#flush()
     */
    public void flush() throws IOException {
        for (int i = curBit; i < 8; i++) {
            curByte[i] = 0;
        }
        curBit = 0;
        writeCurByte();
    }

    private void writeCurByte() throws IOException {
        int toWrite = (curByte[0] << 7) | (curByte[1] << 6) | (curByte[2] << 5)
                | (curByte[3] << 4) | (curByte[4] << 3) | (curByte[5] << 2)
                | (curByte[6] << 1) | curByte[7];
        os.write(toWrite);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ua.org.jplayer.javcodec.h264.H264BitOutputStream#write1Bit(int)
     */
    public void write1Bit(int value) throws IOException {
        Debug.print(value);
        if (curBit == 8) {
            curBit = 0;
            writeCurByte();
        }
        curByte[curBit++] = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ua.org.jplayer.javcodec.h264.H264BitOutputStream#writeNBit(long,
     * int)
     */
    public void writeNBit(long value, int n) throws IOException {
        for (int i = 0; i < n; i++) {
            write1Bit((int) (value >> (n - i - 1)) & 0x1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ua.org.jplayer.javcodec.h264.H264BitOutputStream#writeRemainingZero()
     */
    public void writeRemainingZero() throws IOException {
        writeNBit(0, 8 - curBit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ua.org.jplayer.javcodec.h264.H264BitOutputStream#writeByte(int)
     */
    public void writeByte(int b) throws IOException {
        os.write(b);

    }
}