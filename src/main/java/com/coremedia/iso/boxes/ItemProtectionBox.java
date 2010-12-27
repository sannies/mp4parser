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
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * The Item Protection Box provides an array of item protection information, for use by the Item Information Box.
 *
 * @see com.coremedia.iso.boxes.ItemProtectionBox
 */
public class ItemProtectionBox extends FullBoxContainer {
  int protectionCount;

  public static final String TYPE = "ipro";

  public ItemProtectionBox() {
    super(TYPE);
  }

  public SchemeInformationBox getItemProtectionScheme() {
    if (getBoxes(SchemeInformationBox.class).length > 0) {
      return getBoxes(SchemeInformationBox.class)[0];
    } else {
      return null;
    }
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    parseHeader(in, size);
    protectionCount = in.readUInt16();
    parseBoxes(size, in, boxFactory, lastMovieFragmentBox);
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt16(protectionCount);
    for (Box boxe : boxes) {
      boxe.getBox(os);
    }
  }

  public String getDisplayName() {
    return "Item Protection Box";
  }

}
