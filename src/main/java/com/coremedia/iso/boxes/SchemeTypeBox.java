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
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * The Scheme Type Box identifies the protection scheme. Resides in  a Protection Scheme Information Box or
 * an SRTP Process Box.
 *
 * @see com.coremedia.iso.boxes.SchemeInformationBox
 */
public class SchemeTypeBox extends FullBox {
  public static final String TYPE = "schm";
  byte[] schemeType = new byte[4];
  long schemeVersion;
  String schemeUri = null;

  public SchemeTypeBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public byte[] getSchemeType() {
    return schemeType;
  }

  public long getSchemeVersion() {
    return schemeVersion;
  }

  public String getSchemeUri() {
    return schemeUri;
  }

  public void setSchemeType(byte[] schemeType) {
    assert schemeType != null && schemeType.length == 4 : "SchemeType may not be null or not 4 bytes long";
    this.schemeType = schemeType;
  }

  public void setSchemeVersion(int schemeVersion) {
    this.schemeVersion = schemeVersion;
  }

  public void setSchemeUri(String schemeUri) {
    this.schemeUri = schemeUri;
  }

  protected long getContentSize() {
    return 8 + (((getFlags() & 1) == 1) ? utf8StringLengthInBytes(schemeUri) + 1 : 0);
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    schemeType[0] = (byte) in.readUInt8();
    schemeType[1] = (byte) in.readUInt8();
    schemeType[2] = (byte) in.readUInt8();
    schemeType[3] = (byte) in.readUInt8();
    schemeVersion = in.readUInt32();
    if ((getFlags() & 1) == 1) {
      schemeUri = in.readString();
    }
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.writeUInt8(schemeType[0]);
    isos.writeUInt8(schemeType[1]);
    isos.writeUInt8(schemeType[2]);
    isos.writeUInt8(schemeType[3]);
    isos.writeUInt32(schemeVersion);
    if ((getFlags() & 1) == 1) {
      isos.writeStringZeroTerm(schemeUri);
    }
  }

  public String getDisplayName() {
    return "Schema Type Box";
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Schema Type Box[");
    buffer.append("schemeUri=").append(schemeUri).append("; ");
    buffer.append("schemeType=").append(IsoFile.bytesToFourCC(schemeType)).append("; ");
    buffer.append("schemeVersion=").append(schemeUri).append("; ");
    buffer.append("]");
    return buffer.toString();
  }
}
