package com.coremedia.iso;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Test symmetrie of IsoBufferWrapper and Iso
 */
public class IsoTypeReaderTest {


    @Test
    public void testInt() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer bb = ByteBuffer.allocate(20);

        IsoTypeWriter.writeUInt8(bb, 0);
        IsoTypeWriter.writeUInt8(bb, 255);
        IsoTypeWriter.writeUInt16(bb, 0);
        IsoTypeWriter.writeUInt16(bb, 2 ^ 16 - 1);
        IsoTypeWriter.writeUInt24(bb, 0);
        IsoTypeWriter.writeUInt24(bb, 2 ^ 24 - 1);
        IsoTypeWriter.writeUInt32(bb, 0);
        IsoTypeWriter.writeUInt32(bb, 2 ^ 32 - 1);
        bb.rewind();

        Assert.assertEquals(0, IsoTypeReader.readUInt8(bb));
        Assert.assertEquals(255, IsoTypeReader.readUInt8(bb));
        Assert.assertEquals(0, IsoTypeReader.readUInt16(bb));
        Assert.assertEquals(2 ^ 16 - 1, IsoTypeReader.readUInt16(bb));
        Assert.assertEquals(0, IsoTypeReader.readUInt24(bb));
        Assert.assertEquals(2 ^ 24 - 1, IsoTypeReader.readUInt24(bb));
        Assert.assertEquals(0, IsoTypeReader.readUInt32(bb));
        Assert.assertEquals(2 ^ 32 - 1, IsoTypeReader.readUInt32(bb));
    }

    @Test
    public void testFixedPoint1616() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;


        ByteBuffer bb = ByteBuffer.allocate(8);

        IsoTypeWriter.writeFixedPont1616(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPont1616(bb,fixedPointTest2);
        bb.rewind();

        Assert.assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
        Assert.assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
    }

    @Test
    public void testFixedPoint88() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;
        ByteBuffer bb = ByteBuffer.allocate(4);



        IsoTypeWriter.writeFixedPont88(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPont88(bb, fixedPointTest2);
        bb.rewind();

        Assert.assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
        Assert.assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
    }

}
