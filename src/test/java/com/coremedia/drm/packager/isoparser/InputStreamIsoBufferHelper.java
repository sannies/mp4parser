package com.coremedia.drm.packager.isoparser;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Helps to wrap InputStream in ByteBuffers
 */
public final class InputStreamIsoBufferHelper {
    public static IsoBufferWrapper get(InputStream is, int sliceSize) throws IOException {
        byte[] b = IOUtils.toByteArray(is);
        if (sliceSize <= 0) {
            return InputStreamIsoBufferHelper.get(b);
        } else {
            ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
            int i = 0;
            while (i < b.length) {
                if ((b.length - i) > sliceSize) {
                    buffers.add(ByteBuffer.wrap(b, i, sliceSize).slice());
                    i += sliceSize;
                } else {
                    buffers.add(ByteBuffer.wrap(b, i, b.length - i).slice());
                    i += b.length - i;
                }
            }
            return new IsoBufferWrapperImpl(buffers.toArray(new ByteBuffer[buffers.size()]));
        }
    }

    public static IsoBufferWrapper get(byte[] is) throws IOException {
        return new IsoBufferWrapperImpl(ByteBuffer.wrap(is));
    }

}
