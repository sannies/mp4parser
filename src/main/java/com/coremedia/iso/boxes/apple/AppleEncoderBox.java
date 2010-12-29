package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public final class AppleEncoderBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9too";


  public AppleEncoderBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Encoder Box";
  }

}