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
 * The <code>ProtectionSchemeInformationBox</code> contains all the information required both
 * to understand the encryption transform applied and its parameters, and also to find other
 * information such as the kind and location of the key management system. It also documents the
 * the original (unencrypted) format of the media. The <code>ProtectionSchemeInformationBox</code>
 * is a container box. It is mandatory in a sample entry that uses a code idicating a
 * protected stream.
 *
 * @see com.coremedia.iso.boxes.odf.OmaDrmKeyManagenentSystemBox
 * @see com.coremedia.iso.boxes.sampleentry.AudioSampleEntry#TYPE_ENCRYPTED
 * @see com.coremedia.iso.boxes.sampleentry.VisualSampleEntry#TYPE_ENCRYPTED
 */
public class ProtectionSchemeInformationBox extends AbstractContainerBox {
  public static final String TYPE = "sinf";

  public ProtectionSchemeInformationBox() {
    super(IsoFile.fourCCtoBytes(TYPE));

  }

  public String getDisplayName() {
    return "Protection Scheme Information Box";
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Protection Scheme Information Box[");
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
