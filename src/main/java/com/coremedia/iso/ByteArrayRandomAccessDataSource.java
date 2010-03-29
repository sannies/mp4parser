/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple byte array based implementation for {@link RandomAccessDataSource}.
 */
public class ByteArrayRandomAccessDataSource extends RandomAccessDataSource {
  byte[] content;
  int offset;

  /**
   * Constructs a <code>ByteArrayRandomAccessDataSource</code> with <code>content</code>
   * as underlying datasource. The byte array will not be changed in any way.
   *
   * @param content the data source
   */
  public ByteArrayRandomAccessDataSource(byte[] content) {
    this.content = content;
  }

  /**
   * Constructs a <code>ByteArrayRandomAccessDataSource</code> from the given
   * input stream. The stream is closed after reading.
   *
   * @param is the data source
   */
  public ByteArrayRandomAccessDataSource(InputStream is) throws IOException {
    int i;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] b = new byte[4096];

    try {
      while ((i = is.read(b)) != -1) {
        baos.write(b, 0, i);
      }
    } finally {
      baos.close();
    }
    this.content = baos.toByteArray();
  }

  public void seek(long pos) throws IOException {
    if (pos > Integer.MAX_VALUE) {
      throw new IOException("This implementation cannot handle offsets greater than " + Integer.MAX_VALUE);
    }
    if (pos > (content.length - 1)) {
      throw new IOException("Cannot set offset behind end of file.");
    }
    offset = (int) pos;

  }

  public int read(byte[] b) throws IOException {
    System.arraycopy(content, offset, b, 0, b.length);
    offset += b.length;
    return b.length;
  }


  public int read() throws IOException {
    try {
      return content[offset] < 0 ? content[offset++] + 256 : content[offset++];
    } catch (ArrayIndexOutOfBoundsException out) {
      return -1;
    }
  }


  public int read(byte[] b, int off, int len) throws IOException {
    System.arraycopy(content, offset, b, off, len);
    offset += len;
    return len;
  }

  @Override
  public long length() throws IOException {
    return content.length;
  }

}
