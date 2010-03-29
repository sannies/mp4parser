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

package com.coremedia.iso.mdta;

import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.RandomAccessDataSource;
import com.coremedia.iso.boxes.TrackMetaDataContainer;

import java.io.IOException;
import java.util.List;

/**
 * An object of this class represents a single sample of an IsoFile as in the file read.
 * The sample has a {@link Chunk} as parent.
 *
 * @see Chunk
 * @see Track
 * @see Sample
 */
public final class SampleImpl<T extends TrackMetaDataContainer> implements Sample<T>, Comparable<SampleImpl<T>> {
  private final long offset;
  private final long size;
  private final Chunk<T> parent;
  private final RandomAccessDataSource randomAccessFile;

  public SampleImpl(long offset, long size, Chunk<T> parent, RandomAccessDataSource randomAccessFile) {
    this.randomAccessFile = randomAccessFile;
    this.offset = offset;
    this.size = size;
    this.parent = parent;
  }

  public void getContent(IsoOutputStream os) throws IOException {

    randomAccessFile.seek(offset);
    long written = 0;
    byte[] buffer = new byte[1024];
    while (written < size) {
      int read = randomAccessFile.read(buffer, 0, buffer.length > (size - written) ? (int) (size - written) : buffer.length);
      os.write(buffer, 0, read);
      written += read;
    }
  }

  public long getSize() {
    return size;
  }

  public long getOffset() {
    return offset;
  }

  public String toString() {
    return "Offset: " + calculateOffset() + " Size: " + size + " Chunk: " + parent.getFirstSample().calculateOffset() + " Track: " + parent.getParentTrack().getTrackId();
  }

  public int compareTo(SampleImpl<T> o) {
    return (int) (this.offset - o.offset);
  }

  public Chunk<T> getParent() {
    return parent;
  }

  public long calculateOffset() {
    long offsetFromChunkStart = 0;
    List<Sample<T>> samples = parent.getSamples();
    for (Sample<T> sample : samples) {
      if (!this.equals(sample)) {
        offsetFromChunkStart += sample.getSize();
      }
    }
    return parent.calculateOffset() + offsetFromChunkStart;
  }
}
