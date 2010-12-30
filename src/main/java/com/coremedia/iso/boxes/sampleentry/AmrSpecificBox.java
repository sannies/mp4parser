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

package com.coremedia.iso.boxes.sampleentry;


import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * AMR audio format specific subbox of an audio sample entry.
 *
 * @see com.coremedia.iso.boxes.sampleentry.AudioSampleEntry
 */
public class AmrSpecificBox extends Box {
  public static final String TYPE = "damr";

  private String vendor;
  private int decoderVersion;
  private int modeSet;
  private int modeChangePeriod;
  private int framesPerSample;

  public AmrSpecificBox() {
    super(IsoFile.fourCCtoBytes("damr"));
  }

  public String getVendor() {
    return vendor;
  }

  public int getDecoderVersion() {
    return decoderVersion;
  }

  public int getModeSet() {
    return modeSet;
  }

  public int getModeChangePeriod() {
    return modeChangePeriod;
  }

  public int getFramesPerSample() {
    return framesPerSample;
  }

  public String getDisplayName() {
    return "AMR Specific Box";
  }

  protected long getContentSize() {
    return 9;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    assert size == 9;
    vendor = IsoFile.bytesToFourCC(in.read(4));
    decoderVersion = in.readUInt8();
    modeSet = in.readUInt16();
    modeChangePeriod = in.readUInt8();
    framesPerSample = in.readUInt8();
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(IsoFile.fourCCtoBytes(vendor));
    isos.writeUInt8(decoderVersion);
    isos.writeUInt16(modeSet);
    isos.writeUInt8(modeChangePeriod);
    isos.writeUInt8(framesPerSample);

  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("AmrSpecificBox[vendor=").append(getVendor());
    buffer.append(";decoderVersion=").append(getDecoderVersion());
    buffer.append(";modeSet=").append(getModeSet());
    buffer.append(";modeChangePeriod=").append(getModeChangePeriod());
    buffer.append(";framesPerSample=").append(getFramesPerSample());
    buffer.append("]");
    return buffer.toString();
  }
}
