package com.coremedia.iso;

import java.io.*;
import java.nio.ByteBuffer;

/**
 *
 */
public class RandomAccessFileIsoBufferWrapperImpl implements IsoBufferWrapper {

    public int readBitsRemaining;
    private byte readBitsBuffer;
    RandomAccessFile raf;

    public RandomAccessFileIsoBufferWrapperImpl(File file) throws IOException {

        raf = new RandomAccessFile(file, "r");


    }

    public long position() throws IOException {
        return raf.getChannel().position();
    }

    public void position(long position) throws IOException {
       raf.seek(position);
    }

    public long size() {
        try {
            return raf.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long readUInt64() throws IOException {
        long result = 0;
        // thanks to Erik Nicolas for finding a bug! Cast to long is definitivly needed
        result += readUInt32() << 32;
        if (result < 0) {
            throw new RuntimeException("I don't know how to deal with UInt64! long is not sufficient and I don't want to use BigInt");
        }
        result += readUInt32();

        return result;
    }

    public long readUInt32() throws IOException {
        long result = 0;
        result += ((long) readUInt16()) << 16;
        result += readUInt16();
        return result;
    }

    public int readUInt24() throws IOException {
        int result = 0;
        result += readUInt16() << 8;
        result += readUInt8();
        return result;
    }

    public int readUInt16() throws IOException {
        int result = 0;
        result += readUInt8() << 8;
        result += readUInt8();
        return result;
    }

    public int readUInt8() throws IOException {
        byte b = read();
        return b < 0 ? b + 256 : b;
    }

    public byte[] read(int byteCount) throws IOException {
        byte[] result = new byte[byteCount];
        this.read(result);
        return result;

    }

    public long remaining() throws IOException {
        return raf.length() - raf.getChannel().position();
    }

    public byte read() throws IOException {
        byte[] b = new byte[1];
        read(b);
        return b[0];
    }

    public int read(byte[] b) throws IOException {
        return raf.read(b, 0, b.length);
    }


    public double readFixedPoint1616() throws IOException {
        byte[] bytes = read(4);
        int result = 0;
        result |= ((bytes[0] << 24) & 0xFF000000);
        result |= ((bytes[1] << 16) & 0xFF0000);
        result |= ((bytes[2] << 8) & 0xFF00);
        result |= ((bytes[3]) & 0xFF);
        return ((double) result) / 65536;

    }

    public float readFixedPoint88() throws IOException {
        byte[] bytes = read(2);
        short result = 0;
        result |= ((bytes[0] << 8) & 0xFF00);
        result |= ((bytes[1]) & 0xFF);
        return ((float) result) / 256;
    }

    public String readIso639() throws IOException {
        int bits = readUInt16();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int c = (bits >> (2 - i) * 5) & 0x1f;
            result.append((char) (c + 0x60));
        }
        return result.toString();
    }

    /**
     * Reads a zero terminated string.
     *
     * @return the string read
     * @throws Error in case of an error in the underlying stream
     */
    public String readString() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = read()) != 0) {
            out.write(read);
        }
        try {
            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error("JVM doesn't support UTF-8");
        }

    }

    public String readString(int length) throws IOException {
        byte[] buffer = new byte[length];
        this.read(buffer);
        try {
            return new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error("JVM doesn't support UTF-8");
        }
    }

    public long skip(long n) throws IOException {
        this.position(this.position() + n);
        return n;
    }

    public IsoBufferWrapper getSegment(long startPos, long length) throws IOException {
        assert  length < Integer.MAX_VALUE;
        byte[] bb = new byte[(int) length];
        raf.read(bb);
        return new IsoBufferWrapperImpl(ByteBuffer.wrap(bb));
    }


    public long readUInt32BE() throws IOException {
        long result = 0;
        result += readUInt16BE();
        result += ((long) readUInt16BE()) << 16;
        return result;
    }

    public int readUInt16BE() throws IOException {
        int result = 0;
        result += readUInt8();
        result += readUInt8() << 8;
        return result;
    }

    /**
     * Reads i bits from the underlying buffers.
     * Caveat: this method always consumes full bytes even if just a bit is read!
     *
     * @param i number of bits to read, 31 max
     * @return bitstring value as unsigned int
     */
    public int readBits(int i) throws IOException {
        if (i > 31) {
            //> signed int
            throw new IllegalArgumentException("cannot read more than 31 bits");
        }

        int ret = 0;
        while (i > 8) {
            final int moved = parse8(8) << i - 8;
            ret = ret | moved;
            i -= 8;
        }
        return ret | parse8(i);
    }

    private int parse8(int i) throws IOException {
        if (readBitsRemaining == 0) {
            readBitsBuffer = read();
            readBitsRemaining = 8;
        }

        if (i > readBitsRemaining) {
            final int resultRemaining = i - readBitsRemaining;
            int buffer = (readBitsBuffer & (int) (Math.pow(2, readBitsRemaining) - 1)) << resultRemaining;

            readBitsBuffer = read();
            readBitsRemaining = 8 - resultRemaining;
            final int movedAndMasked = (readBitsBuffer >>> readBitsRemaining) & (int) (Math.pow(2, resultRemaining) - 1);
            return buffer | movedAndMasked;
        } else {
            readBitsRemaining -= i;

            return (readBitsBuffer >>> readBitsRemaining) & (int) (Math.pow(2, i) - 1);
        }
    }

    public int getReadBitsRemaining() {
        return readBitsRemaining;
    }
}
