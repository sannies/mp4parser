package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * iTunes Artist box.
 */
public final class AppleArtistBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9ART";


  public AppleArtistBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Artist";
  }


}
