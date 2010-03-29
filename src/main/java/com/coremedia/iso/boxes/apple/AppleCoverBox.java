package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleCoverBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "covr";


  public AppleCoverBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Cover";
  }


  public void setPng(byte[] pngData) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(0xe);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(pngData);
  }


  public void setJpg(byte[] jpgData) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(0xd);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(jpgData);
  }
}