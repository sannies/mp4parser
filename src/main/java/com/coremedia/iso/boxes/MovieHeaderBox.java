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
 * <code>
 * Box Type: 'mvhd'<br>
 * Container: {@link MovieBox} ('moov')<br>
 * Mandatory: Yes<br>
 * Quantity: Exactly one<br><br>
 * </code>
 * This box defines overall information which is media-independent, and relevant to the entire presentation
 * considered as a whole.
 */
public class MovieHeaderBox extends FullBox {
  private long creationTime;
  private long modificationTime;
  private long timescale;
  private long duration;
  private double rate;
  private float volume;
  private long[] matrix;
  private long nextTrackId;
  public static final String TYPE = "mvhd";

  public MovieHeaderBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public long getCreationTime() {
    return creationTime;
  }

  public long getModificationTime() {
    return modificationTime;
  }

  public long getTimescale() {
    return timescale;
  }

  public long getDuration() {
    return duration;
  }

  public double getRate() {
    return rate;
  }

  public float getVolume() {
    return volume;
  }

  public long[] getMatrix() {
    return matrix;
  }

  public long getNextTrackId() {
    return nextTrackId;
  }

  protected long getContentSize() {
    long contentSize = 0;
    if (getVersion() == 1) {
      contentSize += 28;
    } else {
      contentSize += 16;
    }
    contentSize += 80;
    return contentSize;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);
    if (getVersion() == 1) {
      creationTime = in.readUInt64();
      modificationTime = in.readUInt64();
      timescale = in.readUInt32();
      duration = in.readUInt64();
    } else {
      creationTime = in.readUInt32();
      modificationTime = in.readUInt32();
      timescale = in.readUInt32();
      duration = in.readUInt32();
    }
    rate = in.readFixedPoint1616();
    volume = in.readFixedPoint88();
    in.readUInt16();
    in.readUInt32();
    in.readUInt32();
    matrix = new long[9];
    for (int i = 0; i < 9; i++) {
      matrix[i] = in.readUInt32();
    }
    for (int i = 0; i < 6; i++) {
      in.readUInt32();
    }
    nextTrackId = in.readUInt32();
  }

  public String getDisplayName() {
    return "Movie Header Box";
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("MovieHeaderBox[");
    result.append("creationTime=").append(getCreationTime());
    result.append(";");
    result.append("modificationTime=").append(getModificationTime());
    result.append(";");
    result.append("timescale=").append(getTimescale());
    result.append(";");
    result.append("duration=").append(getDuration());
    result.append(";");
    result.append("rate=").append(getRate());
    result.append(";");
    result.append("volume=").append(getVolume());
    for (int i = 0; i < matrix.length; i++) {
      result.append(";");
      result.append("matrix").append(i).append("=").append(matrix[i]);
    }
    result.append(";");
    result.append("nextTrackId=").append(getNextTrackId());
    result.append("]");
    return result.toString();
  }

  protected void getContent(IsoOutputStream isos) throws IOException {

    if (getVersion() == 1) {
      isos.writeUInt64(creationTime);
      isos.writeUInt64(modificationTime);
      isos.writeUInt32(timescale);
      isos.writeUInt64(duration);
    } else {
      isos.writeUInt32((int) creationTime);
      isos.writeUInt32((int) modificationTime);
      isos.writeUInt32(timescale);
      isos.writeUInt32((int) duration);
    }
    isos.writeFixedPont1616(rate);
    isos.writeFixedPont88(volume);
    isos.writeUInt16(0);
    isos.writeUInt32(0);
    isos.writeUInt32(0);


    for (int i = 0; i < 9; i++) {
      isos.writeUInt32(matrix[i]);
    }
    for (int i = 0; i < 6; i++) {
      isos.writeUInt32(0);
    }
    isos.writeUInt32(nextTrackId);
  }
}
