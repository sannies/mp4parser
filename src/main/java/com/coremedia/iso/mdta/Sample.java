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

import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.TrackMetaDataContainer;

import java.io.IOException;

/**
 * Represents a single sample in an ISO Box file.
 */
public interface Sample<T extends TrackMetaDataContainer> {
  /**
   * Writes the sample's content into the given IsoOutputStream.
   *
   * @param os Stream for writing the sample
   * @throws java.io.IOException if reading the randomAccessFile fails
   */
  void getContent(IsoOutputStream os) throws IOException;

  /**
   * Gets the size of this sample. Used get the size of chunk.
   *
   * @return the sample's size
   */
  long getSize();

  /**
   * Gets the sample's parent which is a chunk.
   *
   * @return the sample's container
   * @see Chunk
   */
  Chunk<T> getParent();

  long calculateOffset();

  boolean isSyncSample();
}
