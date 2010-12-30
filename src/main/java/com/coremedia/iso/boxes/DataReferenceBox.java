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
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The data reference object contains a table of data references (normally URLs) that declare the location(s) of
 * the media data used within the presentation. The data reference index in the sample description ties entries in
 * this table to the samples in the track. A track may be split over several sources in this way.
 * If the flag is set indicating that the data is in the same file as this box, then no string (not even an empty one)
 * shall be supplied in the entry field.
 * The DataEntryBox within the DataReferenceBox shall be either a DataEntryUrnBox or a DataEntryUrlBox.
 *
 * @see com.coremedia.iso.boxes.DataEntryUrlBox
 * @see com.coremedia.iso.boxes.DataEntryUrnBox
 */
public class DataReferenceBox extends FullContainerBox {

  public static final String TYPE = "dref";

  public DataReferenceBox() {
    super(TYPE);

  }

  protected long getContentSize() {
    return super.getContentSize() + 4;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    setVersion(in.readUInt8());
    setFlags(in.readUInt24());
    in.readUInt32();
    List<Box> boxes = new LinkedList<Box>();
    long remainingContentSize = size - 8;
    while (remainingContentSize > 0) {
      Box box = boxParser.parseBox(in, this, lastMovieFragmentBox);
      remainingContentSize -= box.getSize();
      boxes.add(box);
    }
    this.boxes = boxes.toArray(new Box[0]);
  }

  public String getDisplayName() {
    return "Data Reference Box";
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(getBoxes().length);
    super.getContent(os);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("DataReferenceBox[");
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
