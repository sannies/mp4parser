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


import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.Arrays;

/**
 * Actual statistics about the hint track.
 * In addition to the statistics in the hint media header, the hinter may place extra data in a hint statistics box, in
 * the track user-data box. This is a container box with a variety of sub-boxes that it may contain.
 * <code><pre>
 * aligned(8) class hintstatisticsbox extends box('hinf') {
 * }
 * <p/>
 * aligned(8) class hintBytesSent extends box('tpyl') {
 *   uint(64) bytessent;
 * } // total bytes sent, not including RTP headers
 * <p/>
 * aligned(8) class hintBytesSent extends box('totl') {
 *   uint(32) bytessent;
 * } // total bytes sent, including 12-byte RTP headers
 * <p/>
 * aligned(8) class hintPacketsSent extends box('npck') {
 *   uint(32) packetssent;
 * } // total packets sent
 * <p/>
 * aligned(8) class hintBytesSent extends box('tpay') {
 *   uint(32) bytessent;
 * } // total bytes sent, not including RTP headers
 * <p/>
 * aligned(8) class hintmaxrate extends box('maxr') { // maximum data rate
 *   uint(32) period;  // in milliseconds
 *   uint(32) bytes;   // max bytes sent in any period 'period' long
 * }                   // including RTP headers
 * <p/>
 * aligned(8) class hintmediaBytesSent extends box('dmed') {
 *   uint(64) bytessent;
 * } // total bytes sent from media tracks
 * <p/>
 * aligned(8) class hintimmediateBytesSent extends box('dimm') {
 *   uint(64) bytessent;
 * } // total bytes sent immediate mode
 * <p/>
 * aligned(8) class hintrepeatedBytesSent extends box('drep') {
 *   uint(64) bytessent;
 * } // total bytes in repeated packets
 * <p/>
 * aligned(8) class hintminrelativetime extends box('tmin') {
 *   int(32) time;
 * } // smallest relative transmission time, milliseconds
 * <p/>
 * aligned(8) class hintmaxrelativetime extends box('tmax') {
 *   int(32) time;
 * } // largest relative transmission time, milliseconds
 * <p/>
 * aligned(8) class hintlargestpacket extends box('pmax') {
 *   uint(32) bytes;
 * } // largest packet sent, including RTP header
 * <p/>
 * aligned(8) class hintlongestpacket extends box('dmax') {
 * uint(32) time; } // longest packet duration, milliseconds
 * <p/>
 * aligned(8) class hintpayloadID extends box('payt') {
 * uint(32) payloadID; // payload ID used in RTP packets
 * uint(8) count;
 * char rtpmap_string[count];
 * }
 * </pre></code>
 * Note that not all these sub-boxes may be present, and that there may be multiple 'maxr' boxes, covering
 * different periods.
 *
 * @see com.coremedia.iso.boxes.rtp.HintPacketsSentBox
 * @see HintStatisticsBox
 */
public class HintStatisticBoxes extends Box {
  private long bytesSent;

  public static final String TYPE1 = "trpy";
  public static final String TYPE2 = "totl";
  public static final String TYPE3 = "tpyl";
  public static final String TYPE4 = "tpay";

  public static final String TYPE5 = "dmed";
  public static final String TYPE6 = "dimm";
  public static final String TYPE7 = "drep";

  public HintStatisticBoxes(byte[] type) {
    super(type);
  }

  public long getBytesSent() {
    return bytesSent;
  }

  public String getDisplayName() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Hint Bytes Sent Box (");
    if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE1)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE2))) {
      buffer.append("including 12-byte RTP headers");
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE3)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE4))) {
      buffer.append("not including RTP headers");
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE5))) {
      buffer.append("total bytes sent from media tracks");
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE6))) {
      buffer.append("total bytes sent immediate mode");
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE7))) {
      buffer.append("total bytes in repeated packets");
    } else {
      throw new UnsupportedOperationException();
    }
    buffer.append(")");
    return buffer.toString();
  }

  protected long getContentSize() {
    if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE5)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE6)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE7)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE1)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE3))) {
      return 8;
    } else if (
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE2)) ||
                    Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE4))) {
      return 4;
    } else {
      throw new UnsupportedOperationException();
    }

  }

  protected void getContent(IsoOutputStream isos) throws IOException {

    if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE1)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE3)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE5)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE6)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE7))) {
      isos.writeUInt64(bytesSent);
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE2)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE4))) {
      isos.writeUInt32((int) bytesSent);
    } else {
      throw new UnsupportedOperationException();
    }

  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE1)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE3)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE5)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE6)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE7))) {
      bytesSent = in.readUInt64();
    } else if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE2)) ||
            Arrays.equals(getType(), IsoFile.fourCCtoBytes(TYPE4))) {
      bytesSent = in.readUInt32();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public String toString() {
    return "HintStatistic[type=" + IsoFile.bytesToFourCC(getType()) + ";bytes=" + Long.toHexString(bytesSent) + "]";
  }
}
