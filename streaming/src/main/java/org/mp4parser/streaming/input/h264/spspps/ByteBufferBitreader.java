package org.mp4parser.streaming.input.h264.spspps;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferBitreader {
    ByteBuffer buffer;

    int nBit;

    private int currentByte;
    private int nextByte;


    public ByteBufferBitreader(ByteBuffer buffer) {
        this.buffer = buffer;
        currentByte = get();
        nextByte = get();
    }

    public int get() {
        try {
            int i = buffer.get();
            i = i < 0 ? i + 256 : i;
            return i;
        } catch (BufferUnderflowException e) {
            return -1;
        }
    }

    public int read1Bit() throws IOException {
        if (nBit == 8) {
            advance();
            if (currentByte == -1) {
                return -1;
            }
        }
        int res = (currentByte >> (7 - nBit)) & 1;
        nBit++;
        return res;
    }

    private void advance() throws IOException {
        currentByte = nextByte;
        nextByte = get();
        nBit = 0;
    }

    public int readUE() throws IOException {
        int cnt = 0;
        while (read1Bit() == 0) {
            cnt++;
        }

        int res = 0;
        if (cnt > 0) {
            res = (int) ((1 << cnt) - 1 + readNBit(cnt));
        }

        return res;
    }

    public long readNBit(int n) throws IOException {
        if (n > 64)
            throw new IllegalArgumentException("Can not readByte more then 64 bit");

        long val = 0;

        for (int i = 0; i < n; i++) {
            val <<= 1;
            val |= read1Bit();
        }

        return val;
    }

    public boolean readBool() throws IOException {
        return read1Bit() != 0;
    }

    public int readSE() throws IOException {
        int val = readUE();
        int sign = ((val & 0x1) << 1) - 1;
        val = ((val >> 1) + (val & 0x1)) * sign;
        return val;
    }

    public boolean moreRBSPData() throws IOException {
        if (nBit == 8) {
            advance();
        }
        int tail = 1 << (8 - nBit - 1);
        int mask = ((tail << 1) - 1);
        boolean hasTail = (currentByte & mask) == tail;

        return !(currentByte == -1 || (nextByte == -1 && hasTail));
    }


}
