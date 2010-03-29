package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleTrackTitleBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9nam";


  public AppleTrackTitleBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Track Title";
  }


  public void setTrackTitle(String trackTitle) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(trackTitle));
  }

  public String getTrackTitle() {
    return Utf8.convert(appleDataBox.getContent());
  }
}
