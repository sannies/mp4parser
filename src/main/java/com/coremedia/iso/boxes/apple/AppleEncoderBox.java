package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public final class AppleEncoderBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9too";


  public AppleEncoderBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Encoder Box";
  }


  public void setEncoder(String comment) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(comment));
  }

  public String getEncoder() {
    return Utf8.convert(appleDataBox.getContent());
  }
}