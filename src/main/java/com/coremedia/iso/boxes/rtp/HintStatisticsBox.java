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

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.Box;

/**
 * Parent box for various hint statistics. Defined in ISO/IEC 14496-1.
 *
 * @see com.coremedia.iso.boxes.rtp.HintStatisticBoxes
 * @see com.coremedia.iso.boxes.rtp.HintPacketsSentBox
 */
public class HintStatisticsBox extends AbstractContainerBox {
  public static final String TYPE = "hinf";

  public HintStatisticsBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "Hint Statistics Box";
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("HintStatisticsBox[");
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
