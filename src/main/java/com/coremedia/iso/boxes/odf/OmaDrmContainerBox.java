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

package com.coremedia.iso.boxes.odf;

import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullContainerBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The OMA DRM Container box is the container for a single content object in a OMA DCF file.
 * See OMA DCF Specification for details.
 */
public class OmaDrmContainerBox extends FullContainerBox {
  public static final String TYPE = "odrm";

  protected long getHeaderSize() {
    return 20;
  }

  public byte[] getHeader() {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IsoOutputStream ios = new IsoOutputStream(baos);
      ios.writeUInt32(1);
      ios.write(getType());
      ios.writeUInt64(getSize());
      ios.writeUInt8(getVersion());
      ios.writeUInt24(getFlags());

      assert baos.size() == getHeaderSize();
      return baos.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public OmaDrmContainerBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "OMA DRM Container Box";
  }

  /**
   * Gets the <code>OmaDrmDiscreteHeadersBox</code> child box. If none can be
   * found <code>null</code> is returned.
   *
   * @return the <code>OmaDrmDiscreteHeadersBox</code> if any or <code>null</code>
   */
  public OmaDrmDiscreteHeadersBox getOmaDrmDiscreteHeadersBox() {
    for (Box box : boxes) {
      if (box instanceof OmaDrmDiscreteHeadersBox) {
        return (OmaDrmDiscreteHeadersBox) box;
      }
    }
    return null;
  }


  public OmaDrmContentObjectBox getOmaDrmContentObjectBox() {
    for (Box box : boxes) {
      if (box instanceof OmaDrmContentObjectBox) {
        return (OmaDrmContentObjectBox) box;
      }
    }
    return null;
  }
}
