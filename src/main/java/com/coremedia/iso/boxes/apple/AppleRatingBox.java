package com.coremedia.iso.boxes.apple;

/**
 * iTunes Rating Box.
 */
public final class AppleRatingBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "rtng";


  public AppleRatingBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Rating Box";
  }


}
