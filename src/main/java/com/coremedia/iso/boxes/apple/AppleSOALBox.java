package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleSOALBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "soal";


  public AppleSOALBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes SOAL(?) Box";
  }



}