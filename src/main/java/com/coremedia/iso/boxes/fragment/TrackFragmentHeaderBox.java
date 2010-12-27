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

/**
 * aligned(8) class TrackFragmentHeaderBox
 * extends FullBox(�tfhd�, 0, tf_flags){
 * unsigned int(32) track_ID;
 * // all the following are optional fields
 * unsigned int(64) base_data_offset;
 * unsigned int(32) sample_description_index;
 * unsigned int(32) default_sample_duration;
 * unsigned int(32) default_sample_size;
 * unsigned int(32) default_sample_flags
 * }
 */
public class TrackFragmentHeaderBox extends FullBox {
  public static final String TYPE = "tfhd";

  private long trackId;
  private long baseDataOffset;
  private long sampleDescriptionIndex;
  private long defaultSampleDuration;
  private long defaultSampleSize;
  private SampleFlags defaultSampleFlags;
  private boolean durationIsEmpty;

  public TrackFragmentHeaderBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "Track Fragment Header Box";
  }

  protected long getContentSize() {
    long size = 4;
    if ((getFlags() & 0x1) == 1) { //baseDataOffsetPresent
      size += 8;
    }
    if ((getFlags() & 0x2) == 0x2) { //sampleDescriptionIndexPresent
      size += 4;
    }
    if ((getFlags() & 0x8) == 0x8) { //defaultSampleDurationPresent
      size += 4;
    }
    if ((getFlags() & 0x10) == 0x10) { //defaultSampleSizePresent
      size += 4;
    }
    if ((getFlags() & 0x20) == 0x20) { //defaultSampleFlagsPresent
      size += 4;
    }
    return size;
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(trackId);
    if ((getFlags() & 0x1) == 1) { //baseDataOffsetPresent
      os.writeUInt64(baseDataOffset);
    }
    if ((getFlags() & 0x2) == 0x2) { //sampleDescriptionIndexPresent
      os.writeUInt32(sampleDescriptionIndex);
    }
    if ((getFlags() & 0x8) == 0x8) { //defaultSampleDurationPresent
      os.writeUInt32(defaultSampleDuration);
    }
    if ((getFlags() & 0x10) == 0x10) { //defaultSampleSizePresent
      os.writeUInt32(defaultSampleSize);
    }
    if ((getFlags() & 0x20) == 0x20) { //defaultSampleFlagsPresent
      defaultSampleFlags.getContent(os);
    }
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    trackId = in.readUInt32();
    if ((getFlags() & 0x1) == 1) { //baseDataOffsetPresent
      baseDataOffset = in.readUInt64();
    }
    if ((getFlags() & 0x2) == 0x2) { //sampleDescriptionIndexPresent
      sampleDescriptionIndex = in.readUInt32();
    }
    if ((getFlags() & 0x8) == 0x8) { //defaultSampleDurationPresent
      defaultSampleDuration = in.readUInt32();
    }
    if ((getFlags() & 0x10) == 0x10) { //defaultSampleSizePresent
      defaultSampleSize = in.readUInt32();
    }
    if ((getFlags() & 0x20) == 0x20) { //defaultSampleFlagsPresent
      defaultSampleFlags = new SampleFlags(in.readUInt32());
    }
    if ((getFlags() & 0x10000) == 0x10000) { //durationIsEmpty
      durationIsEmpty = true;
    }
  }

  public long getTrackId() {
    return trackId;
  }

  public long getBaseDataOffset() {
    if ((getFlags() & 0x1) == 1) { //baseDataOffsetPresent
      return baseDataOffset;
    } else {
      //todo: fix it. for now 0 is fine. see MovieFragmentBox#parseMdat
      return 0;
      //return getParent().calculateOffset();
      //Todo only works for first tfhd. Subsequent should be "inherited" according to the spec:
      /**
       * the base-dataoffset for the first track in the movie fragment is the position
       * of the first byte of the enclosing Movie Fragment Box, and for second and subsequent track fragments,
       * the default is the end of the data defined by the preceding fragment.
       */
    }
  }

  public long getSampleDescriptionIndex() {
    return sampleDescriptionIndex;
  }

  public long getDefaultSampleDuration() {
    return defaultSampleDuration;
  }

  public long getDefaultSampleSize() {
    return defaultSampleSize;
  }

  public String getDefaultSampleFlags() {
    return defaultSampleFlags.toString();
  }

  public boolean isDurationIsEmpty() {
    return durationIsEmpty;
  }
}
