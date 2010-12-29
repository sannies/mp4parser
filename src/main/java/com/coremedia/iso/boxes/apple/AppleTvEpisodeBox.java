package com.coremedia.iso.boxes.apple;

/**
 * Tv Episode.
 */
public class AppleTvEpisodeBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "tves";


  public AppleTvEpisodeBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes TV Episode Box";
  }
}