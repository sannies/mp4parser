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
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.Arrays;

/**
 * Represents two specifc hint statistic boxes.
 * <code><pre>
 * aligned(8) class hintBytesSent extends box('trpy') {
 *   uint(64) bytessent;
 * } // total bytes sent, including 12-byte RTP headers
 * <p/>
 * aligned(8) class hintPacketsSent extends box('nump') {
 *   uint(64) packetssent;
 * } // total packets sent
 * </pre></code>
 *
 * @see com.coremedia.iso.boxes.rtp.HintStatisticBoxes
 * @see HintStatisticsBox
 */
public class HintPacketsSentBox extends Box {
  private long packetsSent;
  public static final String TYPE1 = "nump";
  public static final String TYPE2 = "npck";

  public HintPacketsSentBox(byte[] type) {
    super(type);
  }

  public long getPacketsSent() {
    return packetsSent;
  }

  public String getDisplayName() {
    return "Hint Packets Sent Box";
  }

  protected long getContentSize() {
    if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("nump"))) {
      return 8;
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("npck"))) {
      return 4;
    } else {
      throw new UnsupportedOperationException();
    }

  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("nump"))) {
      packetsSent = in.readUInt64();
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("npck"))) {
      packetsSent = in.readUInt32();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("nump"))) {
      isos.writeUInt64(packetsSent);
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("npck"))) {
      isos.writeUInt32((int) packetsSent);
    } else {
      throw new UnsupportedOperationException();
    }

  }

  public String toString() {
    return "HintPacketsSentBox[packetsSent=" + getPacketsSent() + "]";
  }
}
