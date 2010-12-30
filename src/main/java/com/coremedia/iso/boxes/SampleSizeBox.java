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
 * This box containes the sample count and a table giving the size in bytes of each sample.
 * Defined in ISO/IEC 14496-12.
 */
public class SampleSizeBox extends FullBox {
  private long sampleSize;
  private long sampleCount;
  private long[] entrySize;
  public static final String TYPE = "stsz";

  public SampleSizeBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  /**
   * Returns the field sample size.
   * If sampleSize > 0 every sample has the same size.
   * If sampleSize == 0 the samples have different size as stated in the entrySize field.
   *
   * @return the sampleSize field
   */
  public long getSampleSize() {
    return sampleSize;
  }

  public void setSampleSize(long sampleSize) {
    this.sampleSize = sampleSize;
  }


  public long getSampleSizeAtIndex(int index) {
    if (sampleSize > 0) {
      return sampleSize;
    } else {
      return entrySize[index];
    }
  }

  public long getSampleCount() {
    return sampleCount;
  }

  public long[] getEntrySize() {
    return entrySize;
  }

  public void setEntrySize(long[] entrySize) {
    this.entrySize = entrySize;
  }

  public void setEntrySize(int index, long singleSampleSize) {
    if (entrySize == null) {

      entrySize = new long[(int) sampleCount];
      this.sampleSize = 0;
    }
    this.entrySize[index] = singleSampleSize;
  }

  public String getDisplayName() {
    return "Sample Size Box";
  }

  protected long getContentSize() {
    return 8 + (sampleSize == 0 ? entrySize.length * 4 : 0);
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {

    assert ((int) size) == size;

    super.parse(in, size, boxParser, lastMovieFragmentBox);
    sampleSize = in.readUInt32();
    sampleCount = in.readUInt32();
    if (sampleCount > Integer.MAX_VALUE) {
      throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE samples!");
    }

    if (sampleSize == 0) {
      entrySize = new long[(int) sampleCount];

      for (int i = 0; i < sampleCount; i++) {
        entrySize[i] = in.readUInt32();
      }
    }
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.writeUInt32(sampleSize);
    isos.writeUInt32(sampleCount);
    if (sampleSize == 0) {
      for (int i = 0; i < sampleCount; i++) {
        isos.writeUInt32(entrySize[i]);
      }
    }

  }

  public String toString() {
    return "SampleSizeBox[sampleSize=" + getSampleSize() + ";sampleCount=" + getSampleCount() + "]";
  }
}
