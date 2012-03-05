package com.coremedia.iso;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Test symmetrie of IsoBufferWrapper and Iso
 */
public class IsoTypeReaderTest extends TestCase {

    public void testIntAsFailedDuringDevelop() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.write(0);
        ios.write(0);
        ios.write(0x50);
        ios.write(0xb0);
        ios.close();
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        System.err.println(IsoTypeReader.readUInt32(bb));

    }

    public void testInt() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.writeUInt8(0);
        ios.writeUInt8(255);
        ios.writeUInt16(0);
        ios.writeUInt16(2 ^ 16 - 1);
        ios.writeUInt24(0);
        ios.writeUInt24(2 ^ 24 - 1);
        ios.writeUInt32(0);
        ios.writeUInt32(2 ^ 32 - 1);
        ios.close();
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
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


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.writeFixedPont1616(fixedPointTest1);
        ios.writeFixedPont1616(fixedPointTest2);
        ios.close();
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
        assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536);
    }

    public void testFixedPoint88() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.writeFixedPont88(fixedPointTest1);
        ios.writeFixedPont88(fixedPointTest2);
        ios.close();
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        assertEquals("fixedPointTest1", fixedPointTest1, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
        assertEquals("fixedPointTest2", fixedPointTest2, IsoTypeReader.readFixedPoint88(bb), 1d / 256);
    }

}
