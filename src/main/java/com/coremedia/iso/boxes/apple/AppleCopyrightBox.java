package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public final class AppleCopyrightBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "cprt";


  public AppleCopyrightBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Copyright Box";
  }


  public void setCopyright(String copyright) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(copyright));
  }

  public String getCopyright() {
    return Utf8.convert(appleDataBox.getContent());
  }
}