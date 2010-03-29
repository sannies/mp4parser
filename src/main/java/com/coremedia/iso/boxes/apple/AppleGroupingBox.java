package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public final class AppleGroupingBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9grp";


  public AppleGroupingBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Grouping Box";
  }


  public void setGrouping(String grouping) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(grouping));
  }

  public String getGrouping() {
    return Utf8.convert(appleDataBox.getContent());
  }
}