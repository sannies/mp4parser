package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleTrackTitleBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9nam";


  public AppleTrackTitleBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Track Title";
  }


}
