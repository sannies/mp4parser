package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleIdBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "apID";


  public AppleIdBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Apple Id Box";
  }


  public void setAppleId(String appleId) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(appleId));
  }

  public String getAppleId() {
    return Utf8.convert(appleDataBox.getContent());
  }

}