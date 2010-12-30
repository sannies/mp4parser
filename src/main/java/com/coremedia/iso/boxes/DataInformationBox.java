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

import com.coremedia.iso.IsoFile;

/**
 * <code>
 * Box Type: 'dinf'<br>
 * Container: {@link com.coremedia.iso.boxes.MediaInformationBox} ('minf')<br>
 * Mandatory: Yes<br>
 * Quantity: Exactly one<br><br></code>
 * The data information box contains objects that declare the location of the media information in a track.
 */
public class DataInformationBox extends AbstractContainerBox {
  public static final String TYPE = "dinf";

  public DataInformationBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "Data Information Box";
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("DataInformationBox[");
    BoxInterface[] boxes = getBoxes();
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
