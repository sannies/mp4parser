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


/**
 * A common base structure to contain general metadata. See ISO/IEC 14496-12 Ch. 8.44.1.
 */
public class MetaBox extends FullContainerBox {

  public static final String TYPE = "meta";

  public MetaBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "Meta Box";
  }
/*     THIS COULD NOT BE TESTED - SO I COMMENT IT OUT
  public void addBox(Box b) {
    HashMap<String, Box> meta = new HashMap<String, Box>();
    for (Box boxe : boxes) {
      meta.put(IsoFile.bytesToFourCC(boxe.getType()), boxe);
    }
    meta.put(IsoFile.bytesToFourCC(b.getType()), b);
    sortBoxes(meta);
  }

  public ItemProtectionBox getItemProtectionBox() {
    if (getBoxes(ItemProtectionBox.class).length > 0) {
      return getBoxes(ItemProtectionBox.class)[0];
    } else {
      return null;
    }
  }


  String[] order = new String[]{"hdlr", "dinf", "ipmc", "iloc", "ipro", "iinf", "xml", "bxml", "pitm"};

  private void sortBoxes(HashMap<String, Box> meta) {
    List<Box> newBoxes = new LinkedList<Box>();
    for (String anOrder : order) {
      Box b = meta.get(anOrder);
      if (b != null) {
        newBoxes.add(b);
      }
    }
    boxes = newBoxes.toArray(new Box[]{});
  }

  public HandlerBox getHandlerBox() {
    if (getBoxes(HandlerBox.class).length > 0) {
      return getBoxes(HandlerBox.class)[0];
    } else {
      return null;
    }
  } */
}
