package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleTrackAuthorBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9wrt";


  public AppleTrackAuthorBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Track Author";
  }


}