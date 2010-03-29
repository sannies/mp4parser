package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleStandardGenreBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "gnre";


  public AppleStandardGenreBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Standard Genre";
  }


  public void setGenre(int genre) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(0);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(new byte[]{(byte) ((genre >> 8) & 0xFF), (byte) ((genre) & 0xFF)});
  }

  public int getGenre() {
    int rc = (((int) appleDataBox.getContent()[0]) & 0xff) << 8;
    rc += (((int) appleDataBox.getContent()[1]) & 0xff);
    return rc;
  }
}