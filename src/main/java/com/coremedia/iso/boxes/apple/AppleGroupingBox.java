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

}