package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public final class AppleAlbumBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9alb";


  public AppleAlbumBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Album Title";
  }


  public void setAlbumTitle(String albumTitle) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(albumTitle));
  }

  public String getAlbumTitle() {
    return Utf8.convert(appleDataBox.getContent());
  }


}