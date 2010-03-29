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

  public int getTvSeason() {
    return appleDataBox.getContent()[0];
  }

  public void setTvSeason(int tvSeason) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(21);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(new byte[]{(byte) (tvSeason & 0xFF)});

  }
}