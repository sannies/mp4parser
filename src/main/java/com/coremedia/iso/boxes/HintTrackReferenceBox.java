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
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the track ID(s) of the media track(s) hinted by this track.
 */
public class HintTrackReferenceBox extends Box {
  private List<Long> tracks;

  public static final String TYPE = "hint";

  public HintTrackReferenceBox() {
    super(IsoFile.fourCCtoBytes(HintTrackReferenceBox.TYPE));
  }

  /**
   * The Track ID field of the 'hint' atom contains the track ID of the media track being hinted. The target track ID
   * can be found in the media track's track header atom ('tkhd'). All media sample data should be taken from
   * the specified media track. There could theoretically be a list of track IDs for a hint track that hinted
   * multiple media tracks, but the current hinter only references one media track per hint track.
   *
   * @return media track being hinted
   */
  public List<Long> getTracks() {
    return tracks;
  }

  protected long getContentSize() {
    return 4 * tracks.size();
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    tracks = new ArrayList<Long>((int) size / 4);
    while (size >= 4) {
      tracks.add(in.readUInt32());
      size -= 4;
    }
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    for (Long track : tracks) {
      isos.writeUInt32(track);
    }
  }

  public String getDisplayName() {
    return "Hint Track Reference Box";
  }

  public String toString() {
    return "HintTrackReferenceBox[tracks=" + tracks + "]";
  }
}
