package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 2/29/12
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class BitWriterBufferTest {
    @Test
    public void testWriteWithinBuffer() {
        ByteBuffer b = ByteBuffer.allocate(2);
        b.put((byte) 0);
        BitWriterBuffer bwb = new BitWriterBuffer(b);
        bwb.writeBits(15, 4);
        Assert.assertEquals("0000000011110000", toString(b));

    }

    @Test
    public void testSimple() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        BitWriterBuffer bitWriterBuffer = new BitWriterBuffer(bb);
        bitWriterBuffer.writeBits(15, 4);
        bb.rewind();
        int test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(15 << 4, test);
    }

    @Test
    public void testSimpleOnByteBorder() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        BitWriterBuffer bitWriterBuffer = new BitWriterBuffer(bb);
        bitWriterBuffer.writeBits(15, 4);
        bitWriterBuffer.writeBits(15, 4);
        bitWriterBuffer.writeBits(15, 4);
        bb.rewind();
        int test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(255, test);
        test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(15 << 4, test);
    }

    @Test
    public void testSimpleCrossByteBorder() {
        ByteBuffer bb = ByteBuffer.allocate(2);
        BitWriterBuffer bitWriterBuffer = new BitWriterBuffer(bb);

        bitWriterBuffer.writeBits(1, 4);
        bitWriterBuffer.writeBits(1, 5);
        bitWriterBuffer.writeBits(1, 3);

        Assert.assertEquals("0001000010010000", toString(bb));
    }

    @Test
    public void testMultiByte() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        BitWriterBuffer bitWriterBuffer = new BitWriterBuffer(bb);
        bitWriterBuffer.writeBits(0, 1);
        bitWriterBuffer.writeBits(65535, 16);
        bb.rewind();
        int test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(127, test);
        test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(255, test);
        test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(1 << 7, test);
    }

    @Test
    public void testPattern() {
        ByteBuffer bb = ByteBuffer.allocate(1);
        BitWriterBuffer bwb = new BitWriterBuffer(bb);
        bwb.writeBits(1, 1);
        bwb.writeBits(1, 2);
        bwb.writeBits(1, 3);
        bwb.writeBits(1, 2);

        Assert.assertEquals("10100101", toString(bb));
    }

    @Test
    public void testWriterReaderRoundTrip() {
        ByteBuffer b = ByteBuffer.allocate(3);
        BitWriterBuffer bwb = new BitWriterBuffer(b);
        bwb.writeBits(1, 1);
        bwb.writeBits(1, 2);
        bwb.writeBits(1, 3);
        bwb.writeBits(1, 4);
        bwb.writeBits(1, 5);
        bwb.writeBits(7, 6);
        b.rewind();

        Assert.assertEquals("101001000100001000111000", toString(b));


    }


    public static String toString(ByteBuffer bb) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bb.limit(); i++) {
            int b = bb.get(i);
            b = b < 0 ? b + 256 : b;


            for (int j = 7; j >= 0; j--) {
                sb.append((b >> j & 1) == 1 ? "1" : "0");
            }

        }
        return sb.toString();
    }
}
