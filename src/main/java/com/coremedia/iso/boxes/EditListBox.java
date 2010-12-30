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

/**
 * <code>
 * Box Type  : 'elst'<br>
 * Container: {@link EditBox}('edts')<br>
 * Mandatory: No<br>
 * Quantity  : Zero or one</code><br><br>
 * This box contains an explicit timeline map. Each entry defines part of the track time-line: by mapping part of
 * the media time-line, or by indicating 'empty' time, or by defining a 'dwell', where a single time-point in the
 * media is held for a period.<br>
 * Note that edits are not restricted to fall on sample times. This means that when entering an edit, it can be
 * necessary to (a) back up to a sync point, and pre-roll from there and then (b) be careful about the duration of
 * the first sample - it might have been truncated if the edit enters it during its normal duration. If this is audio,
 * that frame might need to be decoded, and then the final slicing done. Likewise, the duration of the last sample
 * in an edit might need slicing. <br>
 * Starting offsets for tracks (streams) are represented by an initial empty edit. For example, to play a track from
 * its start for 30 seconds, but at 10 seconds into the presentation, we have the following edit list:<br>
 * <p/>
 * <li>Entry-count = 2</li>
 * <li>Segment-duration = 10 seconds</li>
 * <li>Media-Time = -1</li>
 * <li>Media-Rate = 1</li>
 * <li>Segment-duration = 30 seconds (could be the length of the whole track)</li>
 * <li>Media-Time = 0 seconds</li>
 * <li>Media-Rate = 1</li>
 */
public class EditListBox extends FullBox {
  private long[] segmentDurations;
  private long[] mediaTimes;
  private double[] mediaRates;
  public static final String TYPE = "elst";

  public EditListBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public long[] getSegmentDurations() {
    return segmentDurations;
  }

  public long[] getMediaTimes() {
    return mediaTimes;
  }

  public double[] getMediaRates() {
    return mediaRates;
  }

  public String getDisplayName() {
    return "Edit List Box";
  }

  protected long getContentSize() {
    long contentSize = 4;
    if (getVersion() == 1) {
      contentSize += mediaRates.length * 16;
    } else {
      contentSize += mediaRates.length * 8;
    }
    contentSize += mediaRates.length * 4;
    return contentSize;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);
    long entryCount = in.readUInt32();
    if (entryCount > Integer.MAX_VALUE) {
      throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE entries!");
    }
    segmentDurations = new long[(int) entryCount];
    mediaTimes = new long[(int) entryCount];
    mediaRates = new double[(int) entryCount];
    for (int i = 0; i < entryCount; i++) {
      if (getVersion() == 1) {
        segmentDurations[i] = in.readUInt64();
        mediaTimes[i] = in.readUInt64();
      } else {
        segmentDurations[i] = in.readUInt32();
        mediaTimes[i] = in.readUInt32();
      }
      mediaRates[i] = in.readFixedPoint1616();
    }
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.writeUInt32(segmentDurations.length);
    for (int i = 0; i < segmentDurations.length; i++) {
      if (getVersion() == 1) {
        isos.writeUInt64(segmentDurations[i]);
        isos.writeUInt64(mediaTimes[i]);
      } else {
        isos.writeUInt32((int) segmentDurations[i]);
        isos.writeUInt32((int) mediaTimes[i]);
      }
      isos.writeFixedPont1616(mediaRates[i]);
    }

  }

  public String toString() {
    return "EditListBox[entryCount=" + segmentDurations.length + "]";
  }
}
