package com.coremedia.iso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 */
public class RandomAccessFileIsoBufferWrapperImplTest extends AbstractIsoBufferWrapperTest {
    private IsoBufferWrapper isoBufferWrapper;
    File temp;

    public IsoBufferWrapper getTestIsoBufferWrapper() throws IOException {
        temp = File.createTempFile("RandomAccessFileIsoBufferWrapperImplTest", "test");

        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(getBytesForTest());
        fos.close();
        return new RandomAccessFileIsoBufferWrapperImpl(temp);

    }


}
