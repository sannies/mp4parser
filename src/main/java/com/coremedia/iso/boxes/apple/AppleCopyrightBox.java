package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public final class AppleCopyrightBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "cprt";


  public AppleCopyrightBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Copyright Box";
  }

}