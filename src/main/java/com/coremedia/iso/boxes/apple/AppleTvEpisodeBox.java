package com.coremedia.iso.boxes.apple;

/**
 * Tv Episode.
 */
public class AppleTvEpisodeBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "tven";


  public AppleTvEpisodeBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes TV Episode Box";
  }

  public int getTvEpisode() {
    return appleDataBox.getContent()[0];
  }

  public void setTvEpisode(int tvEpisode) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(21);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(new byte[]{(byte) (tvEpisode & 0xFF)});

  }
}