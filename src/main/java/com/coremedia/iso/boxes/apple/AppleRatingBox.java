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

  public int getRating() {
    return appleDataBox.getContent()[0];
  }

  public void setRating(int gapless) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(21);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(new byte[]{(byte) (gapless & 0xFF)});

  }
}
