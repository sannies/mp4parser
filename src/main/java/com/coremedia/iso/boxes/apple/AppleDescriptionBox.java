package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleDescriptionBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "desc";


  public AppleDescriptionBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Description Box";
  }

}