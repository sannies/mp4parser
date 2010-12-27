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
import java.util.Arrays;

/**
 * The Original Format Box contains the four-character-code of the original untransformed sample description.
 * See ISO/IEC 14496-12 for details.
 *
 * @see ProtectionSchemeInformationBox
 */

public class OriginalFormatBox extends Box {
  public static final String TYPE = "frma";

  private byte[] dataFormat = new byte[4];

  public OriginalFormatBox() {
    super(IsoFile.fourCCtoBytes("frma"));
  }

  public byte[] getDataFormat() {
    return dataFormat;
  }

  public void setDataFormat(byte[] dataFormat) {
    assert dataFormat.length == 4;
    this.dataFormat = dataFormat;
  }

  public String getDisplayName() {
    return "Original Format Box";
  }

  protected long getContentSize() {
    return 4;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    assert size == 4;
    dataFormat[0] = (byte) in.readUInt8();
    dataFormat[1] = (byte) in.readUInt8();
    dataFormat[2] = (byte) in.readUInt8();
    dataFormat[3] = (byte) in.readUInt8();
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.write(dataFormat);
  }

  public String toString() {
    return "OriginalFormatBox[dataFormat=" + Arrays.toString(getDataFormat()) + "]";
  }
}
