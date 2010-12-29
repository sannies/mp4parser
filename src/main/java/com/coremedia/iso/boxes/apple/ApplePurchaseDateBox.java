package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class ApplePurchaseDateBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "purd";


  public ApplePurchaseDateBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Purchase Date Box";
  }



}