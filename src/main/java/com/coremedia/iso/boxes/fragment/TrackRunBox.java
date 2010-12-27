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
import java.util.List;

/**
 * aligned(8) class TrackRunBox
 * extends FullBox(''trun, 0, tr_flags) {
 * unsigned int(32) sample_count;
 * // the following are optional fields
 * signed int(32) data_offset;
 * unsigned int(32) first_sample_flags;
 * // all fields in the following array are optional
 * {
 * unsigned int(32) sample_duration;
 * unsigned int(32) sample_size;
 * unsigned int(32) sample_flags
 * unsigned int(32) sample_composition_time_offset;
 * }[ sample_count ]
 * }
 */

public class TrackRunBox extends FullBox {
  public static final String TYPE = "trun";
  private long sampleCount;
  private int dataOffset;
  private SampleFlags firstSampleFlags;
  private List<Entry> entries = new ArrayList<Entry>();
  private boolean dataOffsetPresent;
  private long realOffset;
  private boolean sampleSizePresent;

  public List<Entry> getEntries() {
    return entries;
  }

  public static class Entry {
    private long sampleDuration;
    private long sampleSize;
    private SampleFlags sampleFlags;
    private long sampleCompositionTimeOffset;


    public long getSampleDuration() {
      return sampleDuration;
    }

    public long getSampleSize() {
      return sampleSize;
    }

    public String getSampleFlags() {
      return sampleFlags.toString();
    }

    public long getSampleCompositionTimeOffset() {
      return sampleCompositionTimeOffset;
    }

    @Override
    public String toString() {
      return "Entry{" +
              "sampleDuration=" + sampleDuration +
              ", sampleSize=" + sampleSize +
              ", sampleFlags=" + sampleFlags +
              ", sampleCompositionTimeOffset=" + sampleCompositionTimeOffset +
              '}';
    }
  }

  public void setRealOffset(long realOffset) {
    this.realOffset = realOffset;
  }

  public long getRealOffset() {
    return realOffset;
  }

  public long[] getSampleOffsets() {
    long[] result = new long[entries.size()];

    offset = 0;
    for (int i = 0; i < result.length; i++) {
      result[i] = offset;
      if (isSampleSizePresent()) {
        offset += entries.get(i).getSampleSize();
      } else {
        offset += ((TrackFragmentBox) getParent()).getTrackFragmentHeaderBox().getDefaultSampleSize();
      }
    }

    return result;
  }

  public long[] getSampleSizes() {
    long[] result = new long[entries.size()];

    for (int i = 0; i < result.length; i++) {
      if (isSampleSizePresent()) {
        result[i] = entries.get(i).getSampleSize();
      } else {
        result[i] = ((TrackFragmentBox) getParent()).getTrackFragmentHeaderBox().getDefaultSampleSize();
      }
    }

    return result;
  }

  public TrackRunBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "Track Fragment Run Box";
  }

  protected long getContentSize() {
    long size = 4;

    if ((getFlags() & 0x1) == 1) { //dataOffsetPresent
      size += 4;
    }
    if ((getFlags() & 0x4) == 0x4) { //firstSampleFlagsPresent
      size += 4;
    }

    for (int i = 0; i < sampleCount; i++) {
      if ((getFlags() & 0x100) == 0x100) { //sampleDurationPresent
        size += 4;
      }
      if ((getFlags() & 0x200) == 0x200) { //sampleSizePresent
        size += 4;
      }
      if ((getFlags() & 0x400) == 0x400) { //sampleFlagsPresent
        size += 4;
      }
      if ((getFlags() & 0x800) == 0x800) { //sampleCompositionTimeOffsetPresent
        size += 4;
      }
    }
    return size;
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(sampleCount);

    if ((getFlags() & 0x1) == 1) { //dataOffsetPresent
      os.writeUInt32(dataOffset);
    }
    if ((getFlags() & 0x4) == 0x4) { //firstSampleFlagsPresent
      firstSampleFlags.getContent(os);
    }

    for (Entry entry : entries) {
      if ((getFlags() & 0x100) == 0x100) { //sampleDurationPresent
        os.writeUInt32(entry.sampleDuration);
      }
      if ((getFlags() & 0x200) == 0x200) { //sampleSizePresent
        os.writeUInt32(entry.sampleSize);
      }
      if ((getFlags() & 0x400) == 0x400) { //sampleFlagsPresent
        entry.sampleFlags.getContent(os);
      }
      if ((getFlags() & 0x800) == 0x800) { //sampleCompositionTimeOffsetPresent
        os.writeUInt32(entry.sampleCompositionTimeOffset);
      }
    }
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    sampleCount = in.readUInt32();

    if ((getFlags() & 0x1) == 1) { //dataOffsetPresent
      dataOffset = (int) in.readUInt32();
      dataOffsetPresent = true;
    }
    if ((getFlags() & 0x4) == 0x4) { //firstSampleFlagsPresent
      firstSampleFlags = new SampleFlags(in.readUInt32());
    }

    for (int i = 0; i < sampleCount; i++) {
      Entry entry = new Entry();
      if ((getFlags() & 0x100) == 0x100) { //sampleDurationPresent
        entry.sampleDuration = in.readUInt32();
      }
      if ((getFlags() & 0x200) == 0x200) { //sampleSizePresent
        entry.sampleSize = in.readUInt32();
        sampleSizePresent = true;
      }
      if ((getFlags() & 0x400) == 0x400) { //sampleFlagsPresent
        entry.sampleFlags = new SampleFlags(in.readUInt32());
      }
      if ((getFlags() & 0x800) == 0x800) { //sampleCompositionTimeOffsetPresent
        entry.sampleCompositionTimeOffset = in.readUInt32();
      }
      entries.add(entry);
    }
  }

  public long getSampleCount() {
    return sampleCount;
  }

  public boolean isDataOffsetPresent() {
    return dataOffsetPresent;
  }

  public boolean isSampleSizePresent() {
    return sampleSizePresent;
  }

  public int getDataOffset() {
    return dataOffset;
  }

  public String getFirstSampleFlags() {
    return firstSampleFlags != null ? firstSampleFlags.toString() : "";
  }

}
