package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
The syntax of the fields defined in this section, specified in ABNF [RFC5234], is as follows:
TfxdBox = TfxdBoxLength TfxdBoxType [TfxdBoxLongLength] TfxdBoxUUID TfxdBoxFields
TfxdBoxChildren
TfxdBoxType = "u" "u" "i" "d"
TfxdBoxLength = BoxLength
TfxdBoxLongLength = LongBoxLength
TfxdBoxUUID = %x6D %x1D %x9B %x05 %x42 %xD5 %x44 %xE6
%x80 %xE2 %x14 %x1D %xAF %xF7 %x57 %xB2
TfxdBoxFields = TfxdBoxVersion
TfxdBoxFlags
TfxdBoxDataFields32 / TfxdBoxDataFields64
TfxdBoxVersion = %x00 / %x01
TfxdBoxFlags = 24*24 RESERVED_BIT
TfxdBoxDataFields32 = FragmentAbsoluteTime32
FragmentDuration32
TfxdBoxDataFields64 = FragmentAbsoluteTime64
FragmentDuration64
FragmentAbsoluteTime64 = UNSIGNED_INT32
FragmentDuration64 = UNSIGNED_INT32
FragmentAbsoluteTime64 = UNSIGNED_INT64
FragmentDuration64 = UNSIGNED_INT64
TfxdBoxChildren = *( VendorExtensionUUIDBox )
 */
//@ExtendedUserType(uuid = "6d1d9b05-42d5-44e6-80e2-141daff757b2")
public class TfxdBox extends AbstractFullBox {
  public long fragmentAbsoluteTime;
  public long fragmentAbsoluteDuration;

  public TfxdBox() {
    super("uuid");
  }

  @Override
  public byte[] getUserType() {
      return new byte[]{(byte) 0x6d, (byte) 0x1d, (byte) 0x9b, (byte) 0x05, (byte) 0x42, (byte) 0xd5, (byte) 0x44,
          (byte) 0xe6, (byte) 0x80, (byte) 0xe2, 0x14, (byte) 0x1d, (byte) 0xaf, (byte) 0xf7, (byte) 0x57, (byte) 0xb2};
  }

  @Override
  protected long getContentSize() {
    return getVersion() == 0x01 ? 16 : 8;
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);

    if (getVersion() == 0x01) {
      fragmentAbsoluteTime = in.readUInt64();
      fragmentAbsoluteDuration = in.readUInt64();
    } else {
      fragmentAbsoluteTime = in.readUInt32();
      fragmentAbsoluteDuration = in.readUInt32();
    }
  }

  @Override
  protected void getContent(IsoOutputStream os) throws IOException {
    if (getVersion() == 0x01) {
      os.writeUInt64(fragmentAbsoluteTime);
      os.writeUInt64(fragmentAbsoluteDuration);
    } else {
      os.writeUInt32(fragmentAbsoluteTime);
      os.writeUInt32(fragmentAbsoluteDuration);
    }
  }

  public long getFragmentAbsoluteTime() {
    return fragmentAbsoluteTime;
  }

  public long getFragmentAbsoluteDuration() {
    return fragmentAbsoluteDuration;
  }
}
