package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public class AppleAlbumArtistBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "aART";


  public AppleAlbumArtistBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Album Artist Box";
  }



}