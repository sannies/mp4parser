package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
The syntax of the fields defined in this section, specified in ABNF [RFC5234], is as follows:
TfrfBox = TfrfBoxLength TfrfBoxType [TfrfBoxLongLength] TfrfBoxUUID TfrfBoxFields
TfrfBoxChildren
TfrfBoxType = "u" "u" "i" "d"
TfrfBoxLength = BoxLength
TfrfBoxLongLength = LongBoxLength
TfrfBoxUUID = %xD4 %x80 %x7E %xF2 %xCA %x39 %x46 %x95
%x8E %x54 %x26 %xCB %x9E %x46 %xA7 %x9F
TfrfBoxFields = TfrfBoxVersion
TfrfBoxFlags
FragmentCount
(1* TfrfBoxDataFields32) / (1* TfrfBoxDataFields64)
TfrfBoxVersion = %x00 / %x01
TfrfBoxFlags = 24*24 RESERVED_BIT
FragmentCount = UINT8
TfrfBoxDataFields32 = FragmentAbsoluteTime32
FragmentDuration32
TfrfBoxDataFields64 = FragmentAbsoluteTime64
FragmentDuration64
FragmentAbsoluteTime64 = UNSIGNED_INT32
FragmentDuration64 = UNSIGNED_INT32
FragmentAbsoluteTime64 = UNSIGNED_INT64
FragmentDuration64 = UNSIGNED_INT64
TfrfBoxChildren = *( VendorExtensionUUIDBox )
 */
public class TfrfBox extends AbstractFullBox {
  public int fragmentCount;
  public List<Entry> entries = new ArrayList<Entry>();

  public TfrfBox() {
    super("uuid");
  }

  @Override
  public byte[] getUserType() {
      return new byte[]{(byte) 0xd4, (byte) 0x80, (byte) 0x7e, (byte) 0xf2, (byte) 0xca, (byte) 0x39, (byte) 0x46,
          (byte) 0x95, (byte) 0x8e, (byte) 0x54, 0x26, (byte) 0xcb, (byte) 0x9e, (byte) 0x46, (byte) 0xa7, (byte) 0x9f};
  }

  @Override
  protected long getContentSize() {
    return 1 + fragmentCount * (getVersion() == 0x01 ? 16 : 8);
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);

    fragmentCount = in.readUInt8();

    for (int i = 0; i < fragmentCount; i++) {
      Entry entry = new Entry();
      if (getVersion() == 0x01) {
        entry.fragmentAbsoluteTime = in.readUInt64();
        entry.fragmentAbsoluteDuration = in.readUInt64();
      } else {
        entry.fragmentAbsoluteTime = in.readUInt32();
        entry.fragmentAbsoluteDuration = in.readUInt32();
      }
      entries.add(entry);
    }
  }

  @Override
  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(fragmentCount);

    for (Entry entry : entries) {
      if (getVersion() == 0x01) {
        os.writeUInt64(entry.fragmentAbsoluteTime);
        os.writeUInt64(entry.fragmentAbsoluteDuration);
      } else {
        os.writeUInt32(entry.fragmentAbsoluteTime);
        os.writeUInt32(entry.fragmentAbsoluteDuration);
      }
    }
  }

  public long getFragmentCount() {
    return fragmentCount;
  }

  public List<Entry> getEntries() {
    return entries;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("TfrfBox");
    sb.append("{fragmentCount=").append(fragmentCount);
    sb.append(", entries=").append(entries);
    sb.append('}');
    return sb.toString();
  }

  public class Entry {
    long fragmentAbsoluteTime;
    long fragmentAbsoluteDuration;

    public long getFragmentAbsoluteTime() {
      return fragmentAbsoluteTime;
    }

    public long getFragmentAbsoluteDuration() {
      return fragmentAbsoluteDuration;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("Entry");
      sb.append("{fragmentAbsoluteTime=").append(fragmentAbsoluteTime);
      sb.append(", fragmentAbsoluteDuration=").append(fragmentAbsoluteDuration);
      sb.append('}');
      return sb.toString();
    }
  }
}
