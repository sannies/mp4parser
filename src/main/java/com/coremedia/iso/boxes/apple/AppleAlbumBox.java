package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleAlbumBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9alb";


  public AppleAlbumBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Album Title";
  }



}