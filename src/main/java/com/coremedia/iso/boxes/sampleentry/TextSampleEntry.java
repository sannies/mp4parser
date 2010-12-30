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

package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxContainer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Entry type for timed text samples defined in the timed text specification (ISO/IEC 14496-17).
 */
public class TextSampleEntry extends SampleEntry implements BoxContainer {

  public static final String TYPE1 = "tx3g";

  public static final String TYPE_ENCRYPTED = "enct";

/*  class TextSampleEntry() extends SampleEntry ('tx3g') {
    unsigned int(32)  displayFlags;
    signed int(8)     horizontal-justification;
    signed int(8)     vertical-justification;
    unsigned int(8)   background-color-rgba[4];
    BoxRecord         default-text-box;
    StyleRecord       default-style;
    FontTableBox      font-table;
  }
  */

  private long displayFlags; // 32 bits
  private int horizontalJustification; // 8 bit
  private int verticalJustification;  // 8 bit
  private byte[] backgroundColorRgba; // 4 bytes

  public TextSampleEntry(byte[] type) {
    super(type);
  }


  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);
    displayFlags = in.readUInt32();
    horizontalJustification = in.readUInt8();
    verticalJustification = in.readUInt8();
    backgroundColorRgba[0] = (byte) in.readUInt8();
    backgroundColorRgba[1] = (byte) in.readUInt8();
    backgroundColorRgba[2] = (byte) in.readUInt8();
    backgroundColorRgba[3] = (byte) in.readUInt8();

    size -= 18;
    ArrayList<Box> someBoxes = new ArrayList<Box>();
    while (size > 0) {
      Box b = boxParser.parseBox(in, this, lastMovieFragmentBox);
      someBoxes.add(b);
      size -= b.getSize();
    }
    boxes = someBoxes.toArray(new Box[someBoxes.size()]);
    assert size == 0 : "After parsing all boxes there are " + size + " bytes left. 0 bytes required";
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

  public Box[] getBoxes() {
    return boxes;
  }

  protected long getContentSize() {
    long contentSize = 18;
    for (Box boxe : boxes) {
      contentSize += boxe.getSize();
    }
    return contentSize;
  }

  public String getDisplayName() {
    return "Text Sample Entry";
  }

  public String toString() {
    return "TextSampleEntry";
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(new byte[6]);
    isos.writeUInt16(getDataReferenceIndex());

    isos.writeUInt32(displayFlags);
    isos.writeUInt8(horizontalJustification);
    isos.writeUInt8(verticalJustification);
    isos.writeUInt8(backgroundColorRgba[0]);
    isos.writeUInt8(backgroundColorRgba[1]);
    isos.writeUInt8(backgroundColorRgba[2]);
    isos.writeUInt8(backgroundColorRgba[3]);

    for (Box boxe : boxes) {
      boxe.getBox(isos);
    }
  }
}
