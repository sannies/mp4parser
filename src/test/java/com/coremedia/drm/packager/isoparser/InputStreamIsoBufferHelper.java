package com.coremedia.drm.packager.isoparser;

import com.coremedia.iso.IsoBufferWrapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Helps to wrap InputStream in ByteBuffers
 */
public final class InputStreamIsoBufferHelper {
  public static IsoBufferWrapper get(InputStream is) throws IOException {
    return new IsoBufferWrapper(ByteBuffer.wrap(IOUtils.toByteArray(is)));
  }

  public static IsoBufferWrapper get(byte[] is) throws IOException {
    return new IsoBufferWrapper(ByteBuffer.wrap(is));
  }

}
