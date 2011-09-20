package com.coremedia.iso;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class IsoBufferWrapperImplTest extends AbstractIsoBufferWrapperTest {


    @Override
    public IsoBufferWrapper getTestIsoBufferWrapper() throws IOException {
        byte[] b = getBytesForTest();
        return new IsoBufferWrapperImpl(new ByteBuffer[]{ByteBuffer.wrap(b, 0, 3).slice(), ByteBuffer.wrap(b, 3, b.length - 3).slice()});

    }
}
