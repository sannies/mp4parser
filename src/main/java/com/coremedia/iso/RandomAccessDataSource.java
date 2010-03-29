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

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstraction for a random access data source. An underlying
 * implementation could be a <code>byte[]</code> or a <code>RandomAccessFile</code>.
 *
 * @see com.coremedia.iso.FileRandomAccessDataSource
 * @see com.coremedia.iso.ByteArrayRandomAccessDataSource
 */
public abstract class RandomAccessDataSource extends InputStream {
  /**
   * Sets the file-pointer offset equivalent, measured from the beginning of this
   * file (or datasource), at which the next read occurs.
   *
   * @param pos the new position for the next read operation.
   */
  public abstract void seek(long pos) throws IOException;

  /**
   * Returns the length of this entity.
   *
   * @return the length of this file, measured in bytes.
   * @throws IOException if an I/O error occurs.
   */
  public native long length() throws IOException;

}
