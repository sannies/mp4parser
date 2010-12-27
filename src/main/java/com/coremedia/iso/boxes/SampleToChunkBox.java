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

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * Samples within the media data are grouped into chunks. Chunks can be of different sizes, and the
 * samples within a chunk can have different sizes. This table can be used to find the chunk that
 * contains a sample, its position, and the associated sample description. Defined in ISO/IEC 14496-12.
 */
public class SampleToChunkBox extends FullBox {
  private long[] firstChunk;
  private long[] samplesPerChunk;
  private long[] sampleDescriptionIndex;
  public static final String TYPE = "stsc";

  public SampleToChunkBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public long[] getFirstChunk() {
    return firstChunk;
  }

  public long[] getSamplesPerChunk() {
    return samplesPerChunk;
  }

  public long[] getSampleDescriptionIndex() {
    return sampleDescriptionIndex;
  }

  public String getDisplayName() {
    return "Sample to Chunk Box";
  }

  protected long getContentSize() {
    return firstChunk.length * 12 + 4;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    long entryCount = in.readUInt32();
    if (entryCount > Integer.MAX_VALUE) {
      throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE entries!");
    }

    firstChunk = new long[(int) entryCount];
    samplesPerChunk = new long[(int) entryCount];
    sampleDescriptionIndex = new long[(int) entryCount];
    for (int i = 0; i < entryCount; i++) {
      firstChunk[i] = in.readUInt32();
      samplesPerChunk[i] = in.readUInt32();
      sampleDescriptionIndex[i] = in.readUInt32();
    }
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    long l = isos.getStreamPosition();
    isos.writeUInt32(firstChunk.length);
    for (int i = 0; i < firstChunk.length; i++) {
      isos.writeUInt32(firstChunk[i]);
      isos.writeUInt32(samplesPerChunk[i]);
      isos.writeUInt32(sampleDescriptionIndex[i]);
    }
    assert getContentSize() == (isos.getStreamPosition() - l);
  }

  public String toString() {
    return "SampleToChunkBox[entryCount=" + firstChunk.length + "]";
  }
}
