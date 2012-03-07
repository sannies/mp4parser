package com.coremedia.iso;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Test symmetrie of IsoBufferWrapper and Iso
 */
public class IsoTypeReaderTest extends TestCase {


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

        assertEquals(0, IsoTypeReader.readUInt8(bb));
        assertEquals(255, IsoTypeReader.readUInt8(bb));
        assertEquals(0, IsoTypeReader.readUInt16(bb));
        assertEquals(2 ^ 16 - 1, IsoTypeReader.readUInt16(bb));
        assertEquals(0, IsoTypeReader.readUInt24(bb));
        assertEquals(2 ^ 24 - 1, IsoTypeReader.readUInt24(bb));
        assertEquals(0, IsoTypeReader.readUInt32(bb));
        assertEquals(2 ^ 32 - 1, IsoTypeReader.readUInt32(bb));
    }

    public void testFixedPoint1616() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;


        ByteBuffer bb = ByteBuffer.allocate(8);

        IsoTypeWriter.writeFixedPont1616(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPont1616(bb,fixedPointTest2);
        bb.rewind();

        assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
        assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
    }

    public void testFixedPoint88() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;
        ByteBuffer bb = ByteBuffer.allocate(4);



        IsoTypeWriter.writeFixedPont88(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPont88(bb, fixedPointTest2);
        bb.rewind();

        assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
        assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
    }

}
