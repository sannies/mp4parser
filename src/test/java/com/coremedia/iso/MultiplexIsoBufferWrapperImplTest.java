package com.coremedia.iso;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class MultiplexIsoBufferWrapperImplTest extends AbstractIsoBufferWrapperTest {
    @Override
    public IsoBufferWrapper getTestIsoBufferWrapper() throws IOException {
        List<IsoBufferWrapper> buffers = new LinkedList<IsoBufferWrapper>();
        byte[] b = getBytesForTest();


        buffers.add(new IsoBufferWrapperImpl(ByteBuffer.wrap(b, 0, 3).slice()));
        buffers.add(new IsoBufferWrapperImpl(ByteBuffer.wrap(b, 3, b.length - 3).slice()));
        return new MultiplexIsoBufferWrapperImpl(buffers);

    }
}
