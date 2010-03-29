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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A <code>RandomAccessFile</code> based implementation for {@link RandomAccessDataSource}.
 *
 * @see java.io.RandomAccessFile
 */
public class FileRandomAccessDataSource extends RandomAccessDataSource {

  RandomAccessFile f;


  public FileRandomAccessDataSource(File f) throws FileNotFoundException {
    this.f = new RandomAccessFile(f, "r");
  }

  public void seek(long pos) throws IOException {
    f.seek(pos);
  }

  public int read(byte[] b) throws IOException {
    return f.read(b);
  }


  public int read(byte[] b, int off, int len) throws IOException {
    return f.read(b, off, len);
  }

  public int read() throws IOException {
    return f.read();
  }

  public void close() throws IOException {
    f.close();
  }

  @Override
  public long length() throws IOException {
    return f.length();
  }
}
