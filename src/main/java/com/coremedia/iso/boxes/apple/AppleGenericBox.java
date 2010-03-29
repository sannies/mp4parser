package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.ContainerBox;

/**
 *
 */
public final class AppleGenericBox extends ContainerBox {
  public static final String TYPE = "----";

  public AppleGenericBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "Some iTunes Generic dunno Box";
  }

}