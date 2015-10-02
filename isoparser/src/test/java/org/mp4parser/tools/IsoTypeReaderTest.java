package org.mp4parser.tools;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Test symmetrie of IsoBufferWrapper and Iso
 */
public class IsoTypeReaderTest {


    @Test
    public void testInt() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(20);

        IsoTypeWriter.writeUInt8(bb, 0);
        IsoTypeWriter.writeUInt8(bb, 255);
        IsoTypeWriter.writeUInt16(bb, 0);
        IsoTypeWriter.writeUInt16(bb, (1 << 16) - 1);
        IsoTypeWriter.writeUInt24(bb, 0);
        IsoTypeWriter.writeUInt24(bb, (1 << 24) - 1);
        IsoTypeWriter.writeUInt32(bb, 0);
        IsoTypeWriter.writeUInt32(bb, (1l << 32) - 1);
        bb.rewind();

        Assert.assertEquals(0, IsoTypeReader.readUInt8(bb));
        Assert.assertEquals(255, IsoTypeReader.readUInt8(bb));
        Assert.assertEquals(0, IsoTypeReader.readUInt16(bb));
        Assert.assertEquals((1 << 16) - 1, IsoTypeReader.readUInt16(bb));
        Assert.assertEquals(0, IsoTypeReader.readUInt24(bb));
        Assert.assertEquals((1 << 24) - 1, IsoTypeReader.readUInt24(bb));
        Assert.assertEquals(0, IsoTypeReader.readUInt32(bb));
        Assert.assertEquals((1l << 32) - 1, IsoTypeReader.readUInt32(bb));
    }

    @Test
    public void testFixedPoint1616() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;


        ByteBuffer bb = ByteBuffer.allocate(8);

        IsoTypeWriter.writeFixedPoint1616(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPoint1616(bb, fixedPointTest2);
        bb.rewind();

        Assert.assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
        Assert.assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
    }

    @Test
    public void testFixedPoint0230() throws IOException {
        final double fixedPointTest1 = 1.13;
        final double fixedPointTest2 = -1.13;


        ByteBuffer bb = ByteBuffer.allocate(8);

        IsoTypeWriter.writeFixedPoint0230(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPoint0230(bb, fixedPointTest2);
        bb.rewind();

        Assert.assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint0230(bb), 1d / 65536);
        Assert.assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint0230(bb), 1d / 65536);
    }

    @Test
    public void testFixedPoint88() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;
        ByteBuffer bb = ByteBuffer.allocate(4);


        IsoTypeWriter.writeFixedPoint88(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPoint88(bb, fixedPointTest2);
        bb.rewind();

        Assert.assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
        Assert.assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
    }

    @Test
    public void testRead4cc() throws IOException {
        ByteBuffer bb = ByteBuffer.wrap("abcd".getBytes());
        String code = IsoTypeReader.read4cc(bb);
        Assert.assertEquals(4, bb.position());
        Assert.assertEquals("abcd", code);

    }

}
