package com.coremedia.iso;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

/**
 * Make sure the IsoBufferWrapper works. Especially the segmentation with
 * multiple ByteBuffers.
 */
public class IsoBufferWrapperMultipleBufferTest extends TestCase {
    private IsoBufferWrapper isoBufferWrapper;

    private IsoBufferWrapper getTestIsoBufferWrapper() {
        byte[] b1 = {1,2,3,4};
        byte[] b2 = {4,5,6};
        return  new IsoBufferWrapper(new ByteBuffer[]{ByteBuffer.wrap(b1, 0, 3), ByteBuffer.wrap(b2)});

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.isoBufferWrapper = getTestIsoBufferWrapper();
    }

    public void testSimple() {
        int i = 1;
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
    }

    public void testSegment() {
       ByteBuffer[] buffers = isoBufferWrapper.getSegment(2,2);
        assertEquals(1, buffers[0].remaining());
        assertEquals(1, buffers[1].remaining());
    }

    public void testPosition() {
        isoBufferWrapper.position(2);
        assertEquals(3, isoBufferWrapper.read());
        isoBufferWrapper.position(4);
        assertEquals(5, isoBufferWrapper.read());
    }
    public void testPositionAfterLast() {
        isoBufferWrapper.position(6);
        assertEquals(6, isoBufferWrapper.position());
        assertEquals(0, isoBufferWrapper.remaining());
    }
    public void testRemaining() {

        assertEquals(6, isoBufferWrapper.remaining());
        isoBufferWrapper.read();
        assertEquals(5, isoBufferWrapper.remaining());
    }

    public void testSkip() {
        isoBufferWrapper.position(1);
        assertEquals(2, isoBufferWrapper.read());
        isoBufferWrapper.skip(2);
        assertEquals(5, isoBufferWrapper.read());
    }

    public void testRead() {
        isoBufferWrapper.position(1);
        byte[] buf = new byte[3];
        isoBufferWrapper.read(buf);
        assertEquals(2, buf[0]);
        assertEquals(3, buf[1]);
        assertEquals(4, buf[2]);

    }

}
