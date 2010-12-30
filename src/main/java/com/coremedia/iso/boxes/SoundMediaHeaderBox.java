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
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

public class SoundMediaHeaderBox extends FullBox {

  public static final String TYPE = "smhd";
  private float balance;

  public SoundMediaHeaderBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public float getBalance() {
    return balance;
  }

  protected long getContentSize() {
    return 2 + 2;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);
    balance = in.readFixedPoint88();
    in.readUInt16();
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.writeFixedPont88(balance);
    isos.writeUInt16(0);
  }

  public String getDisplayName() {
    return "Sound Media Header Box";
  }

  public String toString() {
    return "SoundMediaHeaderBox[balance=" + getBalance() + "]";
  }
}
