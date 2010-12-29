package com.coremedia.iso.boxes.apple;

/**
 * Tv Season.
 */
public final class AppleTvSeasonBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "tvsn";


  public AppleTvSeasonBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes TV Season Box";
  }

}