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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * A <code>FilterInputStream</code> enriched with helper methods to ease writing of
 * Iso specific numbers and strings.
 */
public class IsoBufferWrapper {
  ByteBuffer parent;

  public IsoBufferWrapper(ByteBuffer parent) {
    this.parent = parent;
  }

  public long position() {
    return parent.position();
  }

  public void position(long position) {
    // todo long safe
    parent.position((int) position);
  }

  public long size() {
    return parent.limit();
  }

  public long readUInt64() {
    long result = 0;
    // thanks to Erik Nicolas for finding a bug! Cast to long is definitivly needed
    result += readUInt32() << 32;
    if (result < 0) {
      throw new RuntimeException("I don't know how to deal with UInt64! long is not sufficient and I don't want to use BigInt");
    }
    result += readUInt32();

    return result;
  }

  public long readUInt32() {
    long result = 0;
    result += ((long) readUInt16()) << 16;
    result += readUInt16();
    return result;
  }

  public int readUInt24() {
    int result = 0;
    result += readUInt16() << 8;
    result += readUInt8();
    return result;
  }

  public int readUInt16() {
    int result = 0;
    result += readUInt8() << 8;
    result += readUInt8();
    return result;
  }

  public int readUInt8() {
    return read();
  }

  public byte[] read(int byteCount) {
    byte[] result = new byte[byteCount];
    parent.get(result);
    return result;

  }

  public long remaining() {
    return parent.remaining();
  }

  public int read() {
    byte b = parent.get();
    return b < 0 ? b + 256 : b;
  }

  public int read(byte[] b) {
    return read(b, 0, b.length);
  }

  public int read(byte[] b, int off, int len) {
    parent.get(b, off, len);
    return len;
  }

  public double readFixedPoint1616() {
    byte[] bytes = read(4);
    int result = 0;
    result |= ((bytes[0] << 24) & 0xFF000000);
    result |= ((bytes[1] << 16) & 0xFF0000);
    result |= ((bytes[2] << 8) & 0xFF00);
    result |= ((bytes[3]) & 0xFF);
    return ((double) result) / 65536;

  }

  public float readFixedPoint88() {
    byte[] bytes = read(2);
    short result = 0;
    result |= ((bytes[0] << 8) & 0xFF00);
    result |= ((bytes[1]) & 0xFF);
    return ((float) result) / 256;
  }

  public String readIso639() {
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
   * @in case of an error in the underlying stream
   */
  public String readString() {
//    int size = readUInt8();
//    String result =  new String(read(size), "UTF-8");
//    return result;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int read;
    while ((read = read()) != 0) {
      out.write(read);
    }
    try {
      return out.toString("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new Error("JVM doesn't support UTF-8");
    }

  }

  public String readString(int length) {
    byte[] buffer = new byte[length];
    parent.get(buffer);
    try {
      return new String(buffer, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new Error("JVM doesn't support UTF-8");
    }
  }

  public long skip(long n) {
    // todo make long safe!
    parent.position((int) (parent.position() + n));
    return n;
  }

  public ByteBuffer[] getSegment(long startPos, long length) {
    // todo make long safe
    // todo make it safe across bytebuffers
    parent.position((int) startPos);
    ByteBuffer segment = parent.slice();
    segment.limit((int) length);
    return new ByteBuffer[]{segment};
  }


}
