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


import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.assistui.multiline;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

public class RtpMovieHintInformationBox extends Box {
  public static final String TYPE = "rtp ";

  private String descriptionFormat;

  @multiline(linebreak = "\r\n")
  private String sdpText;

  public RtpMovieHintInformationBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDescriptionFormat() {
    return descriptionFormat;
  }

  public String getSdpText() {
    return sdpText;
  }

  public void setDescriptionFormat(String descriptionFormat) {
    this.descriptionFormat = descriptionFormat;
  }

  public void setSdpText(String sdpText) {
    this.sdpText = sdpText;
  }

  public String getDisplayName() {
    return "RTP Movie Hint Information";
  }


  protected long getContentSize() {
    return utf8StringLengthInBytes(sdpText) + 4;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    descriptionFormat = IsoFile.bytesToFourCC(in.read(4));
    sdpText = in.readString((int) size - 4);
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(IsoFile.fourCCtoBytes(descriptionFormat));
    isos.writeStringNoTerm(sdpText);
  }

  public String toString() {
    return "RtpMovieHintInformationBox[descriptionFormat=" + getDescriptionFormat() + ";sdpText=" + getSdpText() + "]";
  }
}
