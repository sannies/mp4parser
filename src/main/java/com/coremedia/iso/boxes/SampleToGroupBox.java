package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * aligned(8) class SampleToGroupBox
 * extends FullBox('sbgp', version = 0, 0)
 * {
 * unsigned int(32) grouping_type;
 * unsigned int(32) entry_count;
 * for (i=1; i <= entry_count; i++)
 * {
 * unsigned int(32) sample_count;
 * unsigned int(32) group_description_index;
 * }
 * }
 */
public class SampleToGroupBox extends AbstractFullBox {
  public static final String TYPE = "sbgp";
  private long groupingType;
  private long entryCount;
  private List<Entry> entries = new ArrayList<Entry>();

  public SampleToGroupBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  @Override
  protected long getContentSize() {
    return 4 + 4 + entryCount * 8;
  }

  @Override
  public String getDisplayName() {
    return "Sample to Group Box";
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);

    groupingType = in.readUInt32();
    entryCount = in.readUInt32();

    for (int i = 0; i < entryCount; i++) {
      Entry entry = new Entry();
      entry.setSampleCount(in.readUInt32());
      entry.setGroupDescriptionIndex(in.readUInt32());
      entries.add(entry);
    }
  }

  @Override
  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(groupingType);
    os.writeUInt32(entryCount);
    for (Entry entry : entries) {
      os.writeUInt32(entry.getSampleCount());
      os.writeUInt32(entry.getGroupDescriptionIndex());
    }
  }

  public static class Entry {
    private long sampleCount;
    private long groupDescriptionIndex;

    public long getSampleCount() {
      return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
      this.sampleCount = sampleCount;
    }

    public long getGroupDescriptionIndex() {
      return groupDescriptionIndex;
    }

    public void setGroupDescriptionIndex(long groupDescriptionIndex) {
      this.groupDescriptionIndex = groupDescriptionIndex;
    }
  }
}
