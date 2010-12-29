package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleSynopsisBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "ldes";


  public AppleSynopsisBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Synopsis Box";
  }



}