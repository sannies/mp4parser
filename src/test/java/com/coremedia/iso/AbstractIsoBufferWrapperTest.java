package com.coremedia.iso;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Make sure the IsoBufferWrapper works. Especially the segmentation with
 * multiple ByteBuffers.
 */
public abstract class AbstractIsoBufferWrapperTest extends TestCase {
    private IsoBufferWrapper isoBufferWrapper;
    final double fixedPointTest1 = 10.13;
    final double fixedPointTest2 = -10.13;

    public byte[] getBytesForTest() throws IOException {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream ios = new IsoOutputStream(baos);
        ios.write(1);
        ios.write(2);
        ios.write(3);
        ios.write(4);
        ios.write(5);
        ios.write(6);
        ios.writeFixedPont88(fixedPointTest1);
        ios.writeFixedPont88(fixedPointTest2);
        ios.close();
        return baos.toByteArray();
    }

    /**
     * needs to return a buffer with 6 bytes <code>new byte[]{1,2,3,4,5,6}</code>
     *
     * @return
     */
    public abstract IsoBufferWrapper getTestIsoBufferWrapper() throws IOException;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.isoBufferWrapper = getTestIsoBufferWrapper();
    }

    public void testSimple() throws IOException {
        int i = 1;
        assertEquals(i++, isoBufferWrapper.readByte());
        assertEquals(i++, isoBufferWrapper.readByte());
        assertEquals(i++, isoBufferWrapper.readByte());
        assertEquals(i++, isoBufferWrapper.readByte());
        assertEquals(i++, isoBufferWrapper.readByte());
        assertEquals(i++, isoBufferWrapper.readByte());
    }

    public void testSegment() throws IOException {
        IsoBufferWrapper isoBufferWrapper = this.isoBufferWrapper.getSegment(2, 2);
        assertEquals(2, isoBufferWrapper.remaining());
    }

    public void testPosition() throws IOException {
        isoBufferWrapper.position(2);
        assertEquals(3, isoBufferWrapper.readByte());
        isoBufferWrapper.position(4);
        assertEquals(5, isoBufferWrapper.readByte());
    }

    public void testPositionAfterLast() throws IOException {
        long size = isoBufferWrapper.size();
        isoBufferWrapper.position(size);
        assertEquals(size, isoBufferWrapper.position());
        assertEquals(0, isoBufferWrapper.remaining());
    }

    public void testRemaining() throws IOException {
        long size = isoBufferWrapper.size();
        assertEquals(size, isoBufferWrapper.remaining());
        isoBufferWrapper.readByte();
        assertEquals(size - 1, isoBufferWrapper.remaining());
    }

    public void testSkip() throws IOException {
        isoBufferWrapper.position(1);
        assertEquals(2, isoBufferWrapper.readByte());
        isoBufferWrapper.skip(2);
        assertEquals(5, isoBufferWrapper.readByte());
    }

    public void testRead() throws IOException {
        isoBufferWrapper.position(1);
        byte[] buf = new byte[3];
        isoBufferWrapper.read(buf);
        assertEquals(2, buf[0]);
        assertEquals(3, buf[1]);
        assertEquals(4, buf[2]);

    }

    public void testGetSegment() throws IOException {
        isoBufferWrapper.position(0);
        IsoBufferWrapper segment = isoBufferWrapper.getSegment(3, 3);
        segment.position(0);
        Assert.assertEquals(0, isoBufferWrapper.position());
    }

    public void testFixedPoint88() throws IOException {


        isoBufferWrapper.position(6);
        assertEquals("fixedPointTest1", fixedPointTest1, isoBufferWrapper.readFixedPoint88(), 1d / 256);
        assertEquals("fixedPointTest2", fixedPointTest2, isoBufferWrapper.readFixedPoint88(), 1d / 256);
    }

}
