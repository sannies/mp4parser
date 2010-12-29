package com.coremedia.iso.boxes.apple;

/**
 * Gapless Playback.
 */
public final class AppleGaplessPlaybackBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "pgap";


  public AppleGaplessPlaybackBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Gapless Playback";
  }

}
