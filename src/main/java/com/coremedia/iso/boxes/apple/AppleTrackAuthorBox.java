package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleTrackAuthorBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9wrt";


  public AppleTrackAuthorBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Track Author";
  }


  public void setTrackAuthor(String trackAuthor) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(trackAuthor));
  }

  public String getTrackAuthor() {
    return Utf8.convert(appleDataBox.getContent());
  }
}