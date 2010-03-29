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

  public int getGapless() {
    return appleDataBox.getContent()[0];
  }

  public void setGapless(int gapless) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(21);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(new byte[]{(byte) (gapless & 0xFF)});

  }
}
