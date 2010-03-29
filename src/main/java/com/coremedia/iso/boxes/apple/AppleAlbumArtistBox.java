package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public class AppleAlbumArtistBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "aART";


  public AppleAlbumArtistBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Album Artist Box";
  }


  public void setAlbumArtist(String albumArtist) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(1);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(Utf8.convert(albumArtist));
  }

  public String getAlbumArtist() {
    return Utf8.convert(appleDataBox.getContent());
  }
}