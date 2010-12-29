package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleShowBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "tvsh";


  public AppleShowBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes TV Show Box";
  }



}