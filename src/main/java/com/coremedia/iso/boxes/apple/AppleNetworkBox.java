package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleNetworkBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "tvnn";


  public AppleNetworkBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes TV Network Box";
  }



}