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

package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxContainer;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains basic information about the (rtp-) hint samples in this track. Actually this <code>Box</code> class
 * should be named <code>RtpHintSampleEntry</code>.
 */
public class HintSampleEntry extends SampleEntry implements BoxContainer {
  public static final String TYPE1 = "rtp ";

  private int hintTrackVersion;
  private int highestCompatibleVersion;
  private long maxPacketSize;

  public HintSampleEntry(byte[] type) {
    super(type);
  }

  public Box[] getBoxes() {
    return boxes;
  }

  @SuppressWarnings("unchecked")
  public <T extends Box> T[] getBoxes(Class<T> clazz) {
    ArrayList<T> boxesToBeReturned = new ArrayList<T>();
    for (Box boxe : boxes) {
      if (clazz.isInstance(boxe)) {
        boxesToBeReturned.add(clazz.cast(boxe));
      }
    }
    return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
  }

  public int getHintTrackVersion() {
    return hintTrackVersion;
  }

  public int getHighestCompatibleVersion() {
    return highestCompatibleVersion;
  }

  public long getMaxPacketSize() {
    return maxPacketSize;
  }

  protected long getContentSize() {
    long contentLength = 0;
    for (Box box : boxes) {
      contentLength += box.getSize();
    }
    return 16 + contentLength;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    hintTrackVersion = in.readUInt16();
    highestCompatibleVersion = in.readUInt16();
    maxPacketSize = in.readUInt32();
    size -= 16;
    List<Box> boxes = new LinkedList<Box>();
    while (size > 0) {
      Box box = boxFactory.parseBox(in, this, lastMovieFragmentBox);
      size -= box.getSize();
      boxes.add(box);
    }
    this.boxes = boxes.toArray(new Box[0]);
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(new byte[6]);
    isos.writeUInt16(getDataReferenceIndex());
    isos.writeUInt16(hintTrackVersion);
    isos.writeUInt16(highestCompatibleVersion);
    isos.writeUInt32(maxPacketSize);
    for (Box boxe : boxes) {
      boxe.getBox(isos);
    }
  }

  public String getDisplayName() {
    return "Hint Samply Entry";
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("HintSampleEntry[");
    buffer.append("hintTrackVersion=").append(getHintTrackVersion()).append(";");
    buffer.append("highestCompatibleVersion=").append(getHighestCompatibleVersion()).append(";");
    buffer.append("maxPacketSize=").append(getMaxPacketSize());
    Box[] boxes = getBoxes();
    for (int i = 0; i < boxes.length; i++) {
      if (i > 0) {
        buffer.append(";");
      }
      buffer.append(boxes[i].toString());
    }
    buffer.append("]");
    return buffer.toString();
  }
}
