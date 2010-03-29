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

package com.coremedia.iso.mdta;

import com.coremedia.iso.boxes.MediaDataBox;
import com.coremedia.iso.boxes.TrackMetaData;
import com.coremedia.iso.boxes.TrackMetaDataContainer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Track in a Media Data Box.
 */
public class Track<T extends TrackMetaDataContainer> implements Comparable<Track<T>> {
  private final long trackId;
  private final TrackMetaData<T> trackMetaData;
  private MediaDataBox<? extends TrackMetaDataContainer> mediaDataBox;

  public Track(long index, TrackMetaData<T> trackBox, MediaDataBox<? extends TrackMetaDataContainer> mediaDataBox) {
    this.trackId = index;
    this.trackMetaData = trackBox;
    this.mediaDataBox = mediaDataBox;
  }

  /**
   * Gets the tracks trackId according to its occurence in the {@link com.coremedia.iso.boxes.MovieBox}.
   *
   * @return the trackId
   */
  public final long getTrackId() {
    return trackId;
  }

  private List<Chunk<T>> chunks = new LinkedList<Chunk<T>>();

  public List<Chunk<T>> getChunks() {
    return new ArrayList<Chunk<T>>(chunks);
  }

  public TrackMetaData<T> getTrackMetaData() {
    return trackMetaData;
  }

  /**
   * Gets the MediaDataBox that contains the track which is described by this Track.
   *
   * @return the corresponding MediaDataBox
   */
  public MediaDataBox<? extends TrackMetaDataContainer> getMediaDataBox() {
    return mediaDataBox;
  }


  public void addChunk(Chunk<T> chunk) {
    chunks.add(chunk);
  }

  public long getSize() {
    long size = 0;
    for (Chunk<T> chunk : chunks) {
      size += chunk.getSize();
    }
    return size;
  }

  public int compareTo(Track<T> o) {
    return (getTrackId() < o.getTrackId() ? -1 : (getTrackId() == o.getTrackId() ? 0 : 1));
  }

  @Override
  public String toString() {
    return "Track{" +
            "trackId=" + trackId +
            '}';
  }
}
