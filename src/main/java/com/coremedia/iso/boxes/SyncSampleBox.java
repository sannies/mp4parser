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

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * This box provides a compact marking of the random access points withinthe stream. The table is arranged in
 * strictly decreasinf order of sample number. Defined in ISO/IEC 14496-12.
 */
public class SyncSampleBox extends FullBox {
  public static final String TYPE = "stss";

  private long[] sampleNumber;

  public SyncSampleBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  /**
   * Gives the numbers of the samples that are random access points in the stream.
   *
   * @return random access sample numbers.
   */
  public long[] getSampleNumber() {
    return sampleNumber;
  }

  public String getDisplayName() {
    return "Sync Sample Box";
  }

  protected long getContentSize() {
    return sampleNumber.length * 4 + 4;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);
    long entryCount = in.readUInt32();
    if (entryCount > Integer.MAX_VALUE) {
      throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE entries!");
    }

    sampleNumber = new long[(int) entryCount];
    for (int i = 0; i < entryCount; i++) {
      sampleNumber[i] = in.readUInt32();
    }
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.writeUInt32(sampleNumber.length);
    for (long aSampleNumber : sampleNumber) {
      isos.writeUInt32(aSampleNumber);
    }
  }

  public String toString() {
    return "SyncSampleBox[entryCount=" + sampleNumber.length + "]";
  }
}
