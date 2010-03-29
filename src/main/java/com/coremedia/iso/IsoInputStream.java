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

import java.io.*;

/**
 * A <code>FilterInputStream</code> enriched with helper methods to ease writing of
 * Iso specific numbers and strings.
 */
public class IsoInputStream extends FilterInputStream {
  private long streamPosition = 0;


  public IsoInputStream(InputStream is) {
    super(is);
  }

  public long getStreamPosition() {
    return streamPosition;
  }

  public long readUInt64() throws IOException {
    long result = 0;
    // thanks to Erik Nicolas for finding a bug! Cast to long is definitivly needed
    result += readUInt32() << 32;
    if (result < 0) {
      throw new IOException("I don't know how to deal with UInt64! long is not sufficient and I don't want to use BigInt");
    }
    result += readUInt32();

    return result;
  }

  public long readUInt32() throws IOException {
    long result = 0;
    result += ((long) readUInt16()) << 16;
    result += readUInt16();
    return result;
  }

  public int readUInt24() throws IOException {
    int result = 0;
    result += readUInt16() << 8;
    result += readUInt8();
    return result;
  }

  public int readUInt16() throws IOException {
    int result = 0;
    result += readUInt8() << 8;
    result += readUInt8();
    return result;
  }

  public int readUInt8() throws IOException {
    return read();
  }

  public byte[] read(int byteCount) throws IOException {

    byte[] result = new byte[byteCount];

    int read = 0;
    while (read < byteCount) {
      read += read(result, read, byteCount - read);
    }
    return result;
  }

  public int read() throws IOException {
    int result = super.read();
    if (result == -1) {
      throw new EOFException();
    }
    streamPosition++;
    return result;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int result = super.read(b, off, len);
    if (result == -1) {
      throw new EOFException();
    }
    streamPosition += result;
    return result;
  }

  public double readFixedPoint1616() throws IOException {
    byte[] bytes = read(4);
    int result = 0;
    result |= ((bytes[0]<<24) & 0xFF000000);
    result |= ((bytes[1]<<16) & 0xFF0000);
    result |= ((bytes[2]<<8) & 0xFF00);
    result |= ((bytes[3]) & 0xFF);
    return ((double)result)/65536;

  }

  public float readFixedPoint88() throws IOException {
    byte[] bytes = read(2);
    short result = 0;
    result |= ((bytes[0]<<8) & 0xFF00);
    result |= ((bytes[1]) & 0xFF);
    return ((float)result)/256;
  }

  public String readIso639() throws IOException {
    int bits = readUInt16();
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < 3; i++) {
      int c = (bits >> (2 - i) * 5) & 0x1f;
      result.append((char) (c + 0x60));
    }
    return result.toString();
  }

  /**
   * Reads a zero terminated string.
   *
   * @return the string read
   * @throws IOException in case of an error in the underlying stream
   */
  public String readString() throws IOException {
//    int size = readUInt8();
//    String result =  new String(read(size), "UTF-8");
//    return result;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int read;
    while ((read = read()) != 0) {
      out.write(read);
    }
    return new String(out.toByteArray(), "UTF-8");
  }

  public String readString(int length) throws IOException {
    byte[] buffer = new byte[length];
    if (read(buffer) != length) {
      throw new EOFException();
    }
    return new String(buffer, "UTF-8");
  }

  public long skip(long n) throws IOException {
    int skipped = 0;
    while (skipped < n) {
      long l = super.skip(n - skipped);
      if (l == -1) {
        throw new EOFException();
      }
      streamPosition += l;
      skipped += l;
    }
    return n;
  }

}
