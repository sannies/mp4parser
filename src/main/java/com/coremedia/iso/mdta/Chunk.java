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
import com.coremedia.iso.boxes.MediaDataBox;
import com.coremedia.iso.boxes.TrackMetaDataContainer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single chunk containing a couple of samples.
 *
 * @see Track
 * @see Sample
 */
public class Chunk<T extends TrackMetaDataContainer> {
  List<MediaDataBox.SampleHolder<T>> samplesHolders;
  private Track<T> parentTrack;
  private MediaDataBox<T> parentMediaDataBox;

  public Chunk(Track<T> parentTrack, MediaDataBox<T> parentMediaDataBox, int expectedNumberOfSamples) {
    this.samplesHolders = new ArrayList<MediaDataBox.SampleHolder<T>>(expectedNumberOfSamples);
    this.parentTrack = parentTrack;
    this.parentMediaDataBox = parentMediaDataBox;
  }

  public void addSample(MediaDataBox.SampleHolder<T> sample) {
    samplesHolders.add(sample);
  }

  public List<Sample<T>> getSamples() {
    ArrayList<Sample<T>> al = new ArrayList<Sample<T>>(samplesHolders.size());
    for (MediaDataBox.SampleHolder<T> sampleHolder : samplesHolders) {
      al.add(sampleHolder.getSample());
    }
    return al;
  }

  public long getSize() {
    long length = 0;

    for (MediaDataBox.SampleHolder<T> samplesHolder : samplesHolders) {
        Sample<T> currentSample = samplesHolder.getSample();
        long size = currentSample.getSize();

        //todo: not fast
        assert (getSizeByWriting(currentSample) == size) :
                "Size of all samples accumulated is different from the size when written";
        length += size;
    }
    return length;
  }


  /**
   * This method calculates the chunk's size by writing it to a ByteArrayOutputStream.
   * Only called when assertion enabled.
   *
   * @param currentSample
   * @return
   */

  private int getSizeByWriting(Sample<T> currentSample) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      currentSample.getContent(new IsoOutputStream(baos));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return baos.size();
  }

  public Track<T> getParentTrack() {
    return parentTrack;
  }

  public MediaDataBox<T> getParentMediaDataBox() {
    return parentMediaDataBox;
  }

  public Sample<T> getFirstSample() {
    return samplesHolders.get(0).getSample();
  }

  public long calculateOffset() {
    long offsetFromMediaDataBoxStart = getParentMediaDataBox().getHeader().length + getParentMediaDataBox().getDeadBytesBefore().length;

    Sample<T> firstSample = this.getSamples().get(0);
    for (int i = 0; i < parentMediaDataBox.getSampleCount(); i++) {
      Sample<T> currentSample = parentMediaDataBox.getSample(i);
      if (firstSample == currentSample) {
        return offsetFromMediaDataBoxStart;
      } else {
        offsetFromMediaDataBoxStart += currentSample.getSize();
      }

    }
    throw new RuntimeException("Could not find myself...");
  }

  @Override
  public String toString() {
    //System.out.println("Chunk#toString");

    return "Chunk{" +
            "parentTrack=" + parentTrack +
            "; offset=" + calculateOffset() +
            '}';
  }
}
