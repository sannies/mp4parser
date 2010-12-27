/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;
import java.util.ArrayList;

/**
 * aligned(8) class TrackFragmentRandomAccessBox
 * extends FullBox(�tfra�, version, 0) {
 * unsigned int(32) track_ID;
 * const unsigned int(26) reserved = 0;
 * unsigned int(2) length_size_of_traf_num;
 * unsigned int(2) length_size_of_trun_num;
 * unsigned int(2) length_size_of_sample_num;
 * unsigned int(32) number_of_entry;
 * for(i=1; i <= number_of_entry; i++){
 * if(version==1){
 * unsigned int(64) time;
 * unsigned int(64) moof_offset;
 * }else{
 * unsigned int(32) time;
 * unsigned int(32) moof_offset;
 * }
 * unsigned int((length_size_of_traf_num+1) * 8) traf_number;
 * unsigned int((length_size_of_trun_num+1) * 8) trun_number;
 * unsigned int((length_size_of_sample_num+1) * 8) sample_number;
 * }
 * }
 */
public class TrackFragmentRandomAccessBox extends FullBox {
  public static final String TYPE = "tfra";

  private long trackId;
  private int reserved;
  private int lengthSizeOfTrafNum;
  private int lengthSizeOfTrunNum;
  private int lengthSizeOfSampleNum;
  private long numberOfEntries;
  private ArrayList<Entry> entries;

  public TrackFragmentRandomAccessBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "Track Fragment Random Access Box";
  }

  protected long getContentSize() {
    long contentSize = 0;
    contentSize += 4 + 4 /*26 + 2 + 2 + 2 */ + 4;
    if (getVersion() == 1) {
      contentSize += (8 + 8) * numberOfEntries;
    } else {
      contentSize += (4 + 4) * numberOfEntries;
    }
    contentSize += (lengthSizeOfTrafNum + 1) * numberOfEntries;
    contentSize += (lengthSizeOfTrunNum + 1) * numberOfEntries;
    contentSize += (lengthSizeOfSampleNum + 1) * numberOfEntries;
    return contentSize;
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);

    trackId = in.readUInt32();
    long temp = in.readUInt32();
    reserved = (int) (temp >> 6);
    lengthSizeOfTrafNum = (int) (temp & 0x3F) >> 4;
    lengthSizeOfTrunNum = (int) (temp & 0xC) >> 2;
    lengthSizeOfSampleNum = (int) (temp & 0x3);
    numberOfEntries = in.readUInt32();

    entries = new ArrayList<Entry>();

    for (int i = 0; i < numberOfEntries; i++) {
      Entry entry = new Entry();
      if (getVersion() == 1) {
        entry.time = in.readUInt64();
        entry.moofOffset = in.readUInt64();
      } else {
        entry.time = in.readUInt32();
        entry.moofOffset = in.readUInt32();
      }
      entry.trafNumber = getVariable(lengthSizeOfTrafNum, in);
      entry.trunNumber = getVariable(lengthSizeOfTrunNum, in);
      entry.sampleNumber = getVariable(lengthSizeOfSampleNum, in);

      entries.add(entry);
    }
  }

  private long getVariable(long length, IsoBufferWrapper in) throws IOException {
    long ret;
    if (((length + 1) * 8) == 8) {
      ret = in.readUInt8();
    } else if (((length + 1) * 8) == 16) {
      ret = in.readUInt16();
    } else if (((length + 1) * 8) == 24) {
      ret = in.readUInt24();
    } else if (((length + 1) * 8) == 32) {
      ret = in.readUInt32();
    } else if (((length + 1) * 8) == 64) {
      ret = in.readUInt64();
    } else {
      throw new IOException("lengthSize not power of two");
    }
    return ret;
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(trackId);
    long temp;
    temp = reserved << 6;
    temp = temp | (lengthSizeOfTrafNum << 4);
    temp = temp | (lengthSizeOfTrunNum << 2);
    temp = temp | lengthSizeOfSampleNum;
    os.writeUInt32(temp);
    os.writeUInt32(numberOfEntries);

    for (Entry entry : entries) {
      if (getVersion() == 1) {
        os.writeUInt64(entry.time);
        os.writeUInt64(entry.moofOffset);
      } else {
        os.writeUInt32(entry.time);
        os.writeUInt32(entry.moofOffset);
      }
      os.write(toByteArray(lengthSizeOfTrafNum + 1, entry.trafNumber));
      os.write(toByteArray(lengthSizeOfTrunNum + 1, entry.trunNumber));
      os.write(toByteArray(lengthSizeOfSampleNum + 1, entry.sampleNumber));
    }
  }

  private byte[] toByteArray(int length, long value) {
    byte[] b = new byte[length];
    int j = b.length;
    while (j > 0) {
      b[j - 1] = (byte) (value & 0xff);
      value = value >> 8;
      j--;
    }
    return b;
  }

  public long getTrackId() {
    return trackId;
  }

  public int getReserved() {
    return reserved;
  }

  public int getLengthSizeOfTrafNum() {
    return lengthSizeOfTrafNum;
  }

  public int getLengthSizeOfTrunNum() {
    return lengthSizeOfTrunNum;
  }

  public int getLengthSizeOfSampleNum() {
    return lengthSizeOfSampleNum;
  }

  public long getNumberOfEntries() {
    return numberOfEntries;
  }

  public ArrayList<Entry> getEntries() {
    return entries;
  }

  public String getEntriesAsString() {
    StringBuilder sb = new StringBuilder();
    for (Entry entry : entries) {
      sb.append(entry.toString()).append(';');
    }
    return sb.toString();
  }

  public int getEntriesCount() {
    return entries.size();
  }

  public static class Entry {
    public long time;
    public long moofOffset;
    public long trafNumber;
    public long trunNumber;
    public long sampleNumber;

    public long getTime() {
      return time;
    }

    public long getMoofOffset() {
      return moofOffset;
    }

    public long getTrafNumber() {
      return trafNumber;
    }

    public long getTrunNumber() {
      return trunNumber;
    }

    public long getSampleNumber() {
      return sampleNumber;
    }

    @Override
    public String toString() {
      return "Entry{" +
              "time=" + time +
              ", moofOffset=" + moofOffset +
              ", trafNumber=" + trafNumber +
              ", trunNumber=" + trunNumber +
              ", sampleNumber=" + sampleNumber +
              '}';
    }
  }

}
