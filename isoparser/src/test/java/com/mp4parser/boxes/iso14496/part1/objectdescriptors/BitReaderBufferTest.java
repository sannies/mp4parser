package com.mp4parser.boxes.iso14496.part1.objectdescriptors;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class BitReaderBufferTest {
    ByteBuffer testSequence = ByteBuffer.wrap(new byte[]{-1, 0, -1, 0});

    @Test
    public void readFromTheMiddle() {
        ByteBuffer b = ByteBuffer.wrap(new byte[]{0, -1});
        b.get();
        BitReaderBuffer brb = new BitReaderBuffer(b);
        Assert.assertEquals(15, brb.readBits(4));
        Assert.assertEquals(15, brb.readBits(4));

    }

    @Test
    public void testRead_8() {
        BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(testSequence);
        Assert.assertEquals(15, bitReaderBuffer.readBits(4));
        Assert.assertEquals(15, bitReaderBuffer.readBits(4));
        Assert.assertEquals(0, bitReaderBuffer.readBits(4));
        Assert.assertEquals(0, bitReaderBuffer.readBits(4));
    }

    @Test
    public void testReadCrossByte() {
        BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(testSequence);
        Assert.assertEquals(31, bitReaderBuffer.readBits(5));
        Assert.assertEquals(14, bitReaderBuffer.readBits(4));
        Assert.assertEquals(0, bitReaderBuffer.readBits(3));
        Assert.assertEquals(0, bitReaderBuffer.readBits(4));
    }

    @Test
    public void testReadMultiByte() {
        BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(testSequence);
        Assert.assertEquals(510, bitReaderBuffer.readBits(9));
    }

    @Test
    public void testReadMultiByte2() {
        BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(testSequence);
        Assert.assertEquals(0x1fe01, bitReaderBuffer.readBits(17));
    }


    @Test
    public void testRemainingBits() {
        BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(testSequence);
        Assert.assertEquals(32, bitReaderBuffer.remainingBits());
        int six = 6;
        bitReaderBuffer.readBits(six);
        Assert.assertEquals(32 - six, bitReaderBuffer.remainingBits());
    }
}
