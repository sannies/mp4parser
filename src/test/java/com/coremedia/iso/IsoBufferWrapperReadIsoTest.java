package com.coremedia.iso;

import com.coremedia.drm.packager.isoparser.InputStreamIsoBufferHelper;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Test symmetrie of IsoBufferWrapper and Iso
 */
public class IsoBufferWrapperReadIsoTest extends TestCase {

    public void testIntAsFailedDuringDevelop() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.write(0);
        ios.write(0);
        ios.write(0x50);
        ios.write(0xb0);
         ios.close();
        IsoBufferWrapper iis = InputStreamIsoBufferHelper.get(baos.toByteArray());
        System.err.println(iis.readUInt32());

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
        IsoBufferWrapper iis = InputStreamIsoBufferHelper.get(baos.toByteArray());
        assertEquals(0, iis.readUInt8());
        assertEquals(255, iis.readUInt8());
        assertEquals(0, iis.readUInt16());
        assertEquals(2 ^ 16 - 1, iis.readUInt16());
        assertEquals(0, iis.readUInt24());
        assertEquals(2 ^ 24 - 1, iis.readUInt24());
        assertEquals(0, iis.readUInt32());
        assertEquals(2 ^ 32 - 1, iis.readUInt32());
    }

    public void testFixedPoint1616() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.writeFixedPont1616(fixedPointTest1);
        ios.writeFixedPont1616(fixedPointTest2);
        ios.close();
        IsoBufferWrapper iis = InputStreamIsoBufferHelper.get(baos.toByteArray());
        assertEquals("fixedPointTest1", fixedPointTest1, iis.readFixedPoint1616(), 1d / 65536);
        assertEquals("fixedPointTest2", fixedPointTest2, iis.readFixedPoint1616(), 1d / 65536);
    }

    public void testFixedPoint88() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.writeFixedPont88(fixedPointTest1);
        ios.writeFixedPont88(fixedPointTest2);
        ios.close();
        IsoBufferWrapper iis = InputStreamIsoBufferHelper.get(baos.toByteArray());
        assertEquals("fixedPointTest1", fixedPointTest1, iis.readFixedPoint88(), 1d / 256);
        assertEquals("fixedPointTest2", fixedPointTest2, iis.readFixedPoint88(), 1d / 256);
    }

}
