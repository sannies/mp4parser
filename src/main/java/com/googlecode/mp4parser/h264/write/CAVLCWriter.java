package com.googlecode.mp4parser.h264.write;

import com.googlecode.mp4parser.h264.Debug;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A class responsible for outputting exp-Golumb values into binary stream
 *
 * @author Stanislav Vitvitskiy
 */
public class CAVLCWriter extends BitstreamWriter {

    public CAVLCWriter(OutputStream out) {
        super(out);
    }

    public void writeU(int value, int n, String string) throws IOException {
        Debug.print(string + "\t");
        writeNBit(value, n);
        Debug.println("\t" + value);
    }

    public void writeUE(int value) throws IOException {
        int bits = 0;
        int cumul = 0;
        for (int i = 0; i < 15; i++) {
            if (value < cumul + (1 << i)) {
                bits = i;
                break;
            }
            cumul += (1 << i);
        }
        writeNBit(0, bits);
        write1Bit(1);
        writeNBit(value - cumul, bits);
    }

    public void writeUE(int value, String string) throws IOException {
        Debug.print(string + "\t");
        writeUE(value);
        Debug.println("\t" + value);
    }

    public void writeSE(int value, String string) throws IOException {
        Debug.print(string + "\t");
        writeUE((value << 1) * (value < 0 ? -1 : 1) + (value > 0 ? 1 : 0));
        Debug.println("\t" + value);
    }

    public void writeBool(boolean value, String string) throws IOException {
        Debug.print(string + "\t");
        write1Bit(value ? 1 : 0);
        Debug.println("\t" + value);
    }

    public void writeU(int i, int n) throws IOException {
        writeNBit(i, n);
    }

    public void writeNBit(long value, int n, String string) throws IOException {
        Debug.print(string + "\t");
        for (int i = 0; i < n; i++) {
            write1Bit((int) (value >> (n - i - 1)) & 0x1);
        }
        Debug.println("\t" + value);
    }

    public void writeTrailingBits() throws IOException {
        write1Bit(1);
        writeRemainingZero();
        flush();
    }

    public void writeSliceTrailingBits() {
        throw new IllegalStateException("todo");
    }
}