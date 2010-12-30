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

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.BoxInterface;

/**
 * Contains SDP information to be used by a streaming server. <br>
 * Streaming servers using RTSP and SDP usually use SDP as the description format; and there are necessary
 * relationships between the SDP information, and the RTP streams, such as the mapping of payload IDs to
 * mime names. Provision is therefore made for the hinter to leave fragments of SDP information in the file, to
 * assist the server in forming a full SDP description. Note that there are required SDP entries, which the server
 * should also generate. The information here is only partial.
 * SDP information is formatted as a set of boxes within user-data boxes, at both the movie and the track level.
 * The text in the movie-level SDP box should be placed before any media-specific lines (before the first 'm=' in
 * the SDP file).
 * <p/>
 * This Box comes in two flavors:
 * <p/>
 * <b>1. Movie SDP information</b><br>
 * At the movie level, within the user-data ('udta') box, a hint information container box may occur:
 * <code><pre>
 * aligned(8) class moviehintinformation extends box('hnti') {
 * }
 * <p/>
 * aligned(8) class rtpmoviehintinformation extends box('rtp ') {
 *   uint(32) descriptionformat = 'sdp ';
 *   char sdptext[];
 * }
 * </pre></code>
 * The hint information box may contain information for multiple protocols; only RTP is defined here. The RTP
 * box may contain information for various description formats; only SDP is defined here. The sdptext is correctly
 * formatted as a series of lines, each terminated by &lt;crlf>, as required by SDP.<p>
 * <b>2. Track SDP Information</b><br>
 * At the track level, the structure is similar; however, we already know that this track is an RTP hint track, from
 * the sample description. Therefore the child box merely specifies the description format.<br>
 * <code><pre>
 * aligned(8) class trackhintinformation extends box('hnti') {
 * }
 * <p/>
 * aligned(8) class rtptracksdphintinformation extends box('sdp ') {
 *   char sdptext[];
 * }
 * </pre></code>
 * The sdptext is correctly formatted as a series of lines, each terminated by  &lt;crlf>, as required by SDP.
 */
public class HintInformationBox extends AbstractContainerBox {
  public static final String TYPE = "hnti";

  public HintInformationBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "Hint Information Box";
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("HintInformationBox[");
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
