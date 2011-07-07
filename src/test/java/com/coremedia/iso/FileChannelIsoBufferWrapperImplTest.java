package com.coremedia.iso;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 */
public class FileChannelIsoBufferWrapperImplTest extends TestCase {
    private IsoBufferWrapper isoBufferWrapper;
    File temp;

    private IsoBufferWrapper getTestIsoBufferWrapper() throws IOException {
        temp = File.createTempFile("FileChannelIsoBufferWrapperImplTest", "test");
        byte[] b1 = {1, 2, 3, 4, 5, 6};
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(b1);
        fos.close();
        return new FileChannelIsoBufferWrapperImpl(temp);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.isoBufferWrapper = getTestIsoBufferWrapper();
    }

    @Override
    public void tearDown() throws Exception {

    }

    public void testSimple() throws IOException {
        int i = 1;
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
        assertEquals(i++, isoBufferWrapper.read());
    }

    public void testSegment() throws IOException {
        IsoBufferWrapper isoBufferWrapper = this.isoBufferWrapper.getSegment(2, 2);
        assertEquals(2, isoBufferWrapper.remaining());
    }

    public void testPosition() throws IOException {
        isoBufferWrapper.position(2);
        assertEquals(3, isoBufferWrapper.read());
        isoBufferWrapper.position(4);
        assertEquals(5, isoBufferWrapper.read());
    }

    public void testPositionAfterLast() throws IOException {
        isoBufferWrapper.position(6);
        assertEquals(6, isoBufferWrapper.position());
        assertEquals(0, isoBufferWrapper.remaining());
    }

    public void testRemaining() throws IOException {

        assertEquals(6, isoBufferWrapper.remaining());
        isoBufferWrapper.read();
        assertEquals(5, isoBufferWrapper.remaining());
    }

    public void testSkip() throws IOException {
        isoBufferWrapper.position(1);
        assertEquals(2, isoBufferWrapper.read());
        isoBufferWrapper.skip(2);
        assertEquals(5, isoBufferWrapper.read());
    }

    public void testRead() throws IOException {
        isoBufferWrapper.position(1);
        byte[] buf = new byte[3];
        isoBufferWrapper.read(buf);
        assertEquals(2, buf[0]);
        assertEquals(3, buf[1]);
        assertEquals(4, buf[2]);

    }

}
