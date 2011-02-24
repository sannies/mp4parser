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
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Contains basic information about the audio samples in this track. Format-specific information
 * is appened as boxes after the data described in ISO/IEC 14496-12 chapter 8.16.2.
 */
public class AudioSampleEntry extends SampleEntry implements ContainerBox {

  public static final String TYPE1 = "samr";
  public static final String TYPE2 = "sawb";
  public static final String TYPE3 = "mp4a";
  public static final String TYPE4 = "drms";
  public static final String TYPE5 = "alac";
  //public static final String TYPE6 = "mp4s";
  public static final String TYPE7 = "owma";
  public static final String TYPE8 = "ac-3"; /* ETSI TS 102 366 1.2.1 Annex F */

  /**
   * Identifier for an encrypted audio track.
   *
   * @see com.coremedia.iso.boxes.ProtectionSchemeInformationBox
   */
  public static final String TYPE_ENCRYPTED = "enca";

  private int channelCount;
  private int sampleSize;
  private double sampleRate;
  private int soundVersion;
  private int compressionId;
  private int packetSize;
  private long samplesPerPacket;
  private long bytesPerPacket;
  private long bytesPerFrame;
  private long bytesPerSample;


  public AudioSampleEntry(byte[] type) {
    super(type);
  }

  public int getChannelCount() {
    return channelCount;
  }

  public int getSampleSize() {
    return sampleSize;
  }

  public double getSampleRate() {
    return sampleRate;
  }

  public int getSoundVersion() {
    return soundVersion;
  }

  public int getCompressionId() {
    return compressionId;
  }

  public int getPacketSize() {
    return packetSize;
  }

  public long getSamplesPerPacket() {
    return samplesPerPacket;
  }

  public long getBytesPerPacket() {
    return bytesPerPacket;
  }

  public long getBytesPerFrame() {
    return bytesPerFrame;
  }

  public long getBytesPerSample() {
    return bytesPerSample;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
      super.parse(in, size, boxParser, lastMovieFragmentBox);
    //reserved bits - used by qt
    soundVersion = in.readUInt16();

    //reserved
    in.readUInt16();
    in.readUInt32();

    channelCount = in.readUInt16();
    sampleSize = in.readUInt16();
    //reserved bits - used by qt
    compressionId = in.readUInt16();
    //reserved bits - used by qt
    packetSize = in.readUInt16();
    sampleRate = in.readFixedPoint1616();
    //sampleRate = in.readUInt32() >>> 16;

    //more qt stuff - see http://mp4v2.googlecode.com/svn-history/r388/trunk/src/atom_sound.cpp 
    if (soundVersion > 0) {
      samplesPerPacket = in.readUInt32();
      bytesPerPacket = in.readUInt32();
      bytesPerFrame = in.readUInt32();
      bytesPerSample = in.readUInt32();
    }
    if (soundVersion == 2) {
      in.read(20);
    }
    size -= 28;
    ArrayList<Box> someBoxes = new ArrayList<Box>();
    while (size > 8) {
      if (TYPE7.equals(IsoFile.bytesToFourCC(type))) {
        //microsoft garbage
        break;
      }
      Box b = boxParser.parseBox(in, this, lastMovieFragmentBox);
      someBoxes.add(b);
      size -= b.getSize();
    }
    boxes = someBoxes.toArray(new Box[someBoxes.size()]);
    // commented out since it forbids deadbytes		 
    //assert size == 0 : "After parsing all boxes there are " + size + " bytes left. 0 bytes required";
  }


  @SuppressWarnings("unchecked")
  public <T extends Box> T[] getBoxes(Class<T> clazz) {
    ArrayList<T> boxesToBeReturned = new ArrayList<T>();
    for (Box boxe : boxes) {
      if (clazz.isInstance(boxe)) {
        boxesToBeReturned.add(clazz.cast(boxe));
      }
    }
    return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
  }


  public Box[] getBoxes() {
    return boxes;
  }

  protected long getContentSize() {
    long contentSize = 28;
    for (Box boxe : boxes) {
      contentSize += boxe.getSize();
    }
    return contentSize;
  }

  public String getDisplayName() {
    return "Audio Sample Entry";
  }

  public String toString() {
    return "AudioSampleEntry";
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(new byte[6]);
    isos.writeUInt16(getDataReferenceIndex());
    isos.writeUInt32(0);
    isos.writeUInt32(0);
    isos.writeUInt16(getChannelCount());
    isos.writeUInt16(getSampleSize());
    isos.writeUInt16(0);
    isos.writeUInt16(0);
    isos.writeFixedPont1616(getSampleRate());
    for (Box boxe : boxes) {
      boxe.getBox(isos);
    }
  }
}
